package com.loading.service;

import com.loading.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LoadingService {

    // 历史记录存储
    private final Map<Long, LoadingHistory> historyMap = new ConcurrentHashMap<>();
    private final AtomicLong historyIdCounter = new AtomicLong(1);

    public LoadingResult calculateLoading(Truck truck, List<Cargo> cargoList) {
        TruckLoadResult truckResult = calculateSingleTruckLoading(truck, new ArrayList<>(expandCargoList(cargoList)));
        LoadingResult result = new LoadingResult();
        result.setSuccess(truckResult.isSuccess());
        result.setTotalWeight(truckResult.getTotalWeight());
        result.setTotalVolume(truckResult.getTotalVolume());
        result.setUtilizationRate(truckResult.getUtilizationRate());
        result.setPlacedItems(truckResult.getPlacedItems());
        result.setUnplacedItems(new ArrayList<>());
        result.setMessage("单辆车装载完成");
        return result;
    }
    
    public MultiTruckLoadingResult calculateMultiTruckLoading(Truck truck, List<Cargo> cargoList, int maxTrucks) {
        List<CargoItem> items = expandCargoListWithWeight(cargoList);
        List<Cargo> originalCargoList = new ArrayList<>(cargoList);
        
        // 6种排序策略
        List<List<CargoItem>> strategies = generateSortingStrategies(items);
        
        List<TruckLoadResult> bestLayouts = null;
        List<CargoItem> bestUnloadedItems = null;
        double bestTotalUtilization = 0;
        
        // 尝试所有策略，选择最佳的
        for (List<CargoItem> strategyItems : strategies) {
            MultiTruckResult result = solveLoading(strategyItems, truck, maxTrucks, originalCargoList);
            double totalUtilization = result.totalUtilization;
            
            if (bestLayouts == null || totalUtilization > bestTotalUtilization) {
                bestLayouts = result.layouts;
                bestUnloadedItems = result.unloadedItems;
                bestTotalUtilization = totalUtilization;
            }
        }
        
        // 聚合未装载货物
        List<Cargo> bestUnloaded = bestUnloadedItems != null ? aggregateRemaining(bestUnloadedItems, originalCargoList) : new ArrayList<>();
        
        MultiTruckLoadingResult finalResult = new MultiTruckLoadingResult();
        if (bestLayouts != null) {
            finalResult.setTruckResults(bestLayouts);
        }
        finalResult.setUnplacedItems(bestUnloaded);
        finalResult.setTotalTrucksUsed(bestLayouts != null ? bestLayouts.size() : 0);
        finalResult.setSuccess(bestUnloaded.isEmpty());
        finalResult.setMessage(finalResult.isSuccess() 
            ? "所有货物已成功分配到 " + finalResult.getTotalTrucksUsed() + " 辆车"
            : "使用 " + finalResult.getTotalTrucksUsed() + " 辆车已装载，但仍有 " + finalResult.getUnplacedItems().size() + " 件货物未装载");
        
        return finalResult;
    }

    // ========== MS-BFS 算法核心实现 ==========
    
    // 内部类：带重量的货物项
    private static class CargoItem {
        long id;
        String name;
        double length;
        double width;
        double height;
        double weight;
        
        CargoItem(long id, String name, double length, double width, double height, double weight) {
            this.id = id;
            this.name = name;
            this.length = length;
            this.width = width;
            this.height = height;
            this.weight = weight;
        }
    }
    
    // 内部类：放置位置信息
    private static class Placement {
        double x;
        double y;
        double rotW;
        double rotH;
        int segIndex;
        
        Placement(double x, double y, double rotW, double rotH, int segIndex) {
            this.x = x;
            this.y = y;
            this.rotW = rotW;
            this.rotH = rotH;
            this.segIndex = segIndex;
        }
    }
    
    // 内部类：天际线段
    private static class Segment {
        double xStart;
        double xEnd;
        double yHeight;
        
        Segment(double xStart, double xEnd, double yHeight) {
            this.xStart = xStart;
            this.xEnd = xEnd;
            this.yHeight = yHeight;
        }
        
        double getLength() {
            return xEnd - xStart;
        }
    }
    
    // 内部类：多车装载结果
    private static class MultiTruckResult {
        List<TruckLoadResult> layouts;
        List<CargoItem> unloadedItems;
        double totalUtilization;
        
        MultiTruckResult(List<TruckLoadResult> layouts, List<CargoItem> unloadedItems, double totalUtilization) {
            this.layouts = layouts;
            this.unloadedItems = unloadedItems;
            this.totalUtilization = totalUtilization;
        }
    }
    
    // 步骤1：生成6种排序策略
    private List<List<CargoItem>> generateSortingStrategies(List<CargoItem> items) {
        List<List<CargoItem>> strategies = new ArrayList<>();
        
        List<CargoItem> itemsCopy = new ArrayList<>(items);
        
        // 策略1：面积降序
        List<CargoItem> s1 = new ArrayList<>(itemsCopy);
        s1.sort((a, b) -> Double.compare(b.length * b.width, a.length * a.width));
        strategies.add(s1);
        
        // 策略2：最长边降序
        List<CargoItem> s2 = new ArrayList<>(itemsCopy);
        s2.sort((a, b) -> Double.compare(Math.max(b.length, b.width), Math.max(a.length, a.width)));
        strategies.add(s2);
        
        // 策略3：宽度降序（较短边，降序）
        List<CargoItem> s3 = new ArrayList<>(itemsCopy);
        s3.sort((a, b) -> Double.compare(Math.min(b.length, b.width), Math.min(a.length, a.width)));
        strategies.add(s3);
        
        // 策略4：长度降序（较长边，降序）
        List<CargoItem> s4 = new ArrayList<>(itemsCopy);
        s4.sort((a, b) -> Double.compare(Math.max(b.length, b.width), Math.max(a.length, a.width)));
        strategies.add(s4);
        
        // 策略5：密度降序（重量/面积，降序）
        List<CargoItem> s5 = new ArrayList<>(itemsCopy);
        s5.sort((a, b) -> Double.compare(b.weight / (b.length * b.width), a.weight / (a.length * a.width)));
        strategies.add(s5);
        
        // 策略6：随机打乱
        List<CargoItem> s6 = new ArrayList<>(itemsCopy);
        Collections.shuffle(s6);
        strategies.add(s6);
        
        return strategies;
    }
    
    // 步骤2：主装载逻辑
    private MultiTruckResult solveLoading(List<CargoItem> items, Truck truck, int maxTrucks, List<Cargo> originalCargoList) {
        List<CargoItem> remaining = new ArrayList<>(items);
        int truckIndex = 0;
        List<TruckLoadResult> layouts = new ArrayList<>();
        double totalUtilization = 0;
        
        while (!remaining.isEmpty() && truckIndex < maxTrucks) {
            List<Segment> skyline = new ArrayList<>();
            skyline.add(new Segment(0, truck.getLength(), 0));
            
            List<CargoItem> placed = new ArrayList<>();
            List<CargoItem> stillUnplaced = new ArrayList<>();
            double totalWeight = 0;
            double totalArea = 0;
            TruckLoadResult truckResult = new TruckLoadResult();
            truckResult.setTruckNumber(truckIndex + 1);
            truckResult.setTruckSpec(new Truck(truck.getLength(), truck.getWidth(), truck.getHeight(), truck.getMaxWeight()));
            
            // 逐个放置货物
            for (int i = 0; i < remaining.size(); i++) {
                CargoItem item = remaining.get(i);
                
                // 检查高度限制
                if (item.height > truck.getHeight() + 0.1) {
                    stillUnplaced.add(item);
                    continue;
                }
                
                // 尝试两种旋转方向
                boolean placedFlag = false;
                List<double[]> rotations = Arrays.asList(
                    new double[]{item.length, item.width},
                    new double[]{item.width, item.length}
                );
                
                for (double[] rot : rotations) {
                    double rotW = rot[0];
                    double rotH = rot[1];
                    
                    // 边界检查
                    if (rotW > truck.getLength() + 0.1 || rotH > truck.getWidth() + 0.1) {
                        continue;
                    }
                    
                    Placement pos = placeItem(skyline, rotW, rotH, truck.getLength(), truck.getWidth());
                    if (pos != null && (totalWeight + item.weight <= truck.getMaxWeight() + 0.1)) {
                        // 更新天际线
                        updateSkyline(skyline, pos.segIndex, pos.rotW, pos.rotH, pos.x);
                        
                        // 添加到已放置列表
                        PlacedItem placedItem = new PlacedItem(
                            item.id,
                            item.name,
                            pos.x,
                            0,
                            pos.y,
                            pos.rotW,
                            pos.rotH,
                            item.height,
                            (rotW == item.length && rotH == item.width) ? 0 : 90
                        );
                        truckResult.getPlacedItems().add(placedItem);
                        
                        totalWeight += item.weight;
                        totalArea += pos.rotW * pos.rotH;
                        placed.add(item);
                        placedFlag = true;
                        break;
                    }
                }
                
                // 回填优化：如果当前货物放不下，尝试与后续最多3个货物交换顺序
                if (!placedFlag) {
                    int swapCount = Math.min(3, remaining.size() - i - 1);
                    boolean swapped = false;
                    
                    for (int j = 1; j <= swapCount; j++) {
                        int swapIndex = i + j;
                        CargoItem swapItem = remaining.get(swapIndex);
                        
                        // 交换位置
                        remaining.set(i, swapItem);
                        remaining.set(swapIndex, item);
                        
                        // 重新尝试放置当前货物（交换后的位置）
                        CargoItem currentAfterSwap = remaining.get(i);
                        boolean canPlace = false;
                        
                        for (double[] rot : rotations) {
                            double rotW = rot[0];
                            double rotH = rot[1];
                            
                            if (rotW > truck.getLength() + 0.1 || rotH > truck.getWidth() + 0.1) {
                                continue;
                            }
                            
                            Placement pos = placeItem(skyline, rotW, rotH, truck.getLength(), truck.getWidth());
                            if (pos != null && (totalWeight + currentAfterSwap.weight <= truck.getMaxWeight() + 0.1)) {
                                // 更新天际线
                                updateSkyline(skyline, pos.segIndex, pos.rotW, pos.rotH, pos.x);
                                
                                // 添加到已放置列表
                                PlacedItem placedItem = new PlacedItem(
                                    currentAfterSwap.id,
                                    currentAfterSwap.name,
                                    pos.x,
                                    0,
                                    pos.y,
                                    pos.rotW,
                                    pos.rotH,
                                    currentAfterSwap.height,
                                    (rotW == currentAfterSwap.length && rotH == currentAfterSwap.width) ? 0 : 90
                                );
                                truckResult.getPlacedItems().add(placedItem);
                                
                                totalWeight += currentAfterSwap.weight;
                                totalArea += pos.rotW * pos.rotH;
                                placed.add(currentAfterSwap);
                                canPlace = true;
                                swapped = true;
                                break;
                            }
                        }
                        
                        if (canPlace) {
                            break;
                        } else {
                            // 交换回来
                            remaining.set(i, item);
                            remaining.set(swapIndex, swapItem);
                        }
                    }
                    
                    if (!swapped) {
                        stillUnplaced.add(item);
                    }
                }
            }
            
            // 计算面积利用率
            double utilization = totalArea / (truck.getLength() * truck.getWidth()) * 100;
            truckResult.setTotalWeight(totalWeight);
            truckResult.setTotalVolume(totalArea * truck.getHeight());
            truckResult.setUtilizationRate(utilization);
            truckResult.setSuccess(stillUnplaced.isEmpty());
            totalUtilization += utilization;
            
            layouts.add(truckResult);
            remaining = stillUnplaced;
            truckIndex++;
        }
        
        return new MultiTruckResult(layouts, remaining, totalUtilization);
    }
    
    // 步骤3：放置货物逻辑（MS-BFS 核心）
    private Placement placeItem(List<Segment> skyline, double itemW, double itemH, double L, double W) {
        int bestIndex = -1;
        double bestY = Double.MAX_VALUE;
        double bestXRemain = Double.MAX_VALUE;
        double bestYRemain = Double.MAX_VALUE;
        
        for (int i = 0; i < skyline.size(); i++) {
            Segment seg = skyline.get(i);
            
            // 检查是否可以放置
            double segLength = seg.xEnd - seg.xStart;
            if (itemW <= segLength + 0.1 && itemH <= (W - seg.yHeight) + 0.1) {
                double currentY = seg.yHeight;
                double currentXRemain = segLength - itemW;
                double currentYRemain = (W - seg.yHeight) - itemH;
                
                // 评分规则：
                // 1. y越小越好（主键）
                // 2. 水平剩余越小越好（次键）
                // 3. 垂直剩余越小越好（第三键）
                if (bestIndex == -1 ||
                    currentY < bestY - 0.1 ||
                    (Math.abs(currentY - bestY) <= 0.1 && currentXRemain < bestXRemain - 0.1) ||
                    (Math.abs(currentY - bestY) <= 0.1 && Math.abs(currentXRemain - bestXRemain) <= 0.1 && currentYRemain < bestYRemain - 0.1)) {
                    bestIndex = i;
                    bestY = currentY;
                    bestXRemain = currentXRemain;
                    bestYRemain = currentYRemain;
                }
            }
        }
        
        if (bestIndex != -1) {
            Segment bestSeg = skyline.get(bestIndex);
            return new Placement(bestSeg.xStart, bestSeg.yHeight, itemW, itemH, bestIndex);
        }
        
        return null;
    }
    
    // 步骤4：更新天际线
    private void updateSkyline(List<Segment> skyline, int segIndex, double itemW, double itemH, double xPos) {
        Segment oldSeg = skyline.remove(segIndex);
        double newY = oldSeg.yHeight + itemH;
        double xEndPos = xPos + itemW;
        
        List<Segment> newSegments = new ArrayList<>();
        
        // 左侧段
        if (xPos > oldSeg.xStart + 0.1) {
            newSegments.add(new Segment(oldSeg.xStart, xPos, oldSeg.yHeight));
        }
        
        // 中间段（新高度）
        newSegments.add(new Segment(xPos, xEndPos, newY));
        
        // 右侧段
        if (xEndPos < oldSeg.xEnd - 0.1) {
            newSegments.add(new Segment(xEndPos, oldSeg.xEnd, oldSeg.yHeight));
        }
        
        // 插入新段
        skyline.addAll(segIndex, newSegments);
        
        // 合并相邻同高段
        mergeSegments(skyline);
        
        // 按 xStart 排序
        skyline.sort(Comparator.comparingDouble(s -> s.xStart));
    }
    
    // 合并相邻同高段
    private void mergeSegments(List<Segment> skyline) {
        if (skyline.size() <= 1) return;
        
        List<Segment> merged = new ArrayList<>();
        Segment current = skyline.get(0);
        
        for (int i = 1; i < skyline.size(); i++) {
            Segment next = skyline.get(i);
            
            // 检查是否可以合并：y相等且x相邻
            if (Math.abs(current.yHeight - next.yHeight) <= 0.1 &&
                Math.abs(current.xEnd - next.xStart) <= 0.1) {
                current = new Segment(current.xStart, next.xEnd, current.yHeight);
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        
        skyline.clear();
        skyline.addAll(merged);
    }

    // ========== 辅助方法 ==========
    
    private List<CargoItem> expandCargoListWithWeight(List<Cargo> cargoList) {
        List<CargoItem> expanded = new ArrayList<>();
        long idCounter = 1;
        for (Cargo cargo : cargoList) {
            for (int i = 0; i < cargo.getQuantity(); i++) {
                CargoItem item = new CargoItem(
                    idCounter++,
                    cargo.getName(),
                    cargo.getLength(),
                    cargo.getWidth(),
                    cargo.getHeight(),
                    cargo.getWeight()
                );
                expanded.add(item);
            }
        }
        return expanded;
    }
    
    private List<Cargo> expandCargoList(List<Cargo> cargoList) {
        List<Cargo> expanded = new ArrayList<>();
        long idCounter = 1;
        for (Cargo cargo : cargoList) {
            for (int i = 0; i < cargo.getQuantity(); i++) {
                Cargo item = new Cargo(
                    idCounter++,
                    cargo.getName(),
                    cargo.getLength(),
                    cargo.getWidth(),
                    cargo.getHeight(),
                    cargo.getWeight(),
                    1
                );
                expanded.add(item);
            }
        }
        return expanded;
    }
    
    private List<Cargo> aggregateRemaining(List<CargoItem> remainingItems, List<Cargo> originalList) {
        Map<String, Integer> nameToCount = new HashMap<>();
        Map<String, Cargo> nameToOriginal = new HashMap<>();
        
        for (Cargo c : originalList) {
            nameToOriginal.put(c.getName(), c);
        }
        
        for (CargoItem c : remainingItems) {
            nameToCount.put(c.name, nameToCount.getOrDefault(c.name, 0) + 1);
        }
        
        List<Cargo> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : nameToCount.entrySet()) {
            Cargo original = nameToOriginal.get(entry.getKey());
            result.add(new Cargo(
                original.getId(),
                original.getName(),
                original.getLength(),
                original.getWidth(),
                original.getHeight(),
                original.getWeight(),
                entry.getValue()
            ));
        }
        return result;
    }

    // ========== 兼容方法（仅用于单辆车） ==========
    
    private TruckLoadResult calculateSingleTruckLoading(Truck truck, List<Cargo> items) {
        List<CargoItem> itemList = new ArrayList<>();
        long idCounter = 1;
        for (Cargo c : items) {
            itemList.add(new CargoItem(
                c.getId() != null ? c.getId() : idCounter++,
                c.getName(),
                c.getLength(),
                c.getWidth(),
                c.getHeight(),
                c.getWeight()
            ));
        }
        
        List<List<CargoItem>> strategies = generateSortingStrategies(itemList);
        TruckLoadResult bestResult = null;
        double bestUtilization = 0;
        
        for (List<CargoItem> strategyItems : strategies) {
            List<Segment> skyline = new ArrayList<>();
            skyline.add(new Segment(0, truck.getLength(), 0));
            
            TruckLoadResult truckResult = new TruckLoadResult();
            double totalWeight = 0;
            double totalArea = 0;
            
            for (CargoItem item : strategyItems) {
                boolean placed = false;
                for (double[] rot : Arrays.asList(
                    new double[]{item.length, item.width},
                    new double[]{item.width, item.length}
                )) {
                    Placement pos = placeItem(skyline, rot[0], rot[1], truck.getLength(), truck.getWidth());
                    if (pos != null && totalWeight + item.weight <= truck.getMaxWeight()) {
                        updateSkyline(skyline, pos.segIndex, pos.rotW, pos.rotH, pos.x);
                        PlacedItem placedItem = new PlacedItem(
                            item.id, item.name, pos.x, 0, pos.y, pos.rotW, pos.rotH, item.height,
                            (rot[0] == item.length && rot[1] == item.width) ? 0 : 90
                        );
                        truckResult.getPlacedItems().add(placedItem);
                        totalWeight += item.weight;
                        totalArea += pos.rotW * pos.rotH;
                        placed = true;
                        break;
                    }
                }
            }
            
            double utilization = totalArea / (truck.getLength() * truck.getWidth()) * 100;
            truckResult.setTotalWeight(totalWeight);
            truckResult.setTotalVolume(totalArea * truck.getHeight());
            truckResult.setUtilizationRate(utilization);
            truckResult.setSuccess(true);
            
            if (bestResult == null || utilization > bestUtilization) {
                bestResult = truckResult;
                bestUtilization = utilization;
            }
        }
        
        return bestResult;
    }

    public ValidationResult validateCargoList(List<Cargo> cargoList) {
        if (cargoList == null || cargoList.isEmpty()) {
            return new ValidationResult(false, "货物列表不能为空");
        }

        for (Cargo cargo : cargoList) {
            if (cargo.getName() == null || cargo.getName().trim().isEmpty()) {
                return new ValidationResult(false, "货物名称不能为空");
            }
            if (cargo.getLength() <= 0 || cargo.getWidth() <= 0 || cargo.getHeight() <= 0) {
                return new ValidationResult(false, "货物尺寸必须大于0");
            }
            if (cargo.getWeight() <= 0) {
                return new ValidationResult(false, "货物重量必须大于0");
            }
            if (cargo.getQuantity() <= 0) {
                return new ValidationResult(false, "货物数量必须大于0");
            }
        }
        return new ValidationResult(true, "验证通过");
    }

    public Truck getDefaultTruck() {
        return new Truck(1200, 240, 260, 20000);
    }

    // ========== 历史记录管理 ==========
    
    public LoadingHistory saveHistory(MultiTruckLoadingResult result, Truck truck, List<Cargo> cargoList, int truckCount) {
        LoadingHistory history = new LoadingHistory();
        history.setId(historyIdCounter.getAndIncrement());
        history.setCreateTime(new Date());
        history.setTruck(truck);
        history.setCargoList(cargoList);
        history.setTruckCount(truckCount);
        history.setResult(result);
        historyMap.put(history.getId(), history);
        return history;
    }

    public List<LoadingHistory> getAllHistory() {
        return new ArrayList<>(historyMap.values());
    }

    public LoadingHistory getHistoryById(Long id) {
        return historyMap.get(id);
    }

    public boolean deleteHistory(Long id) {
        return historyMap.remove(id) != null;
    }

    // ========== 兼容内部类（保持原样） ==========

    private static class PlacementOld {
        Space space;
        double rotatedLength;
        double rotatedWidth;
        double rotatedHeight;
        int rotation;

        PlacementOld(Space space, double l, double w, double h, int rotation) {
            this.space = space;
            this.rotatedLength = l;
            this.rotatedWidth = w;
            this.rotatedHeight = h;
            this.rotation = rotation;
        }
    }

    private static class RotationOptionOld {
        double length;
        double width;
        double height;
        int rotation;

        RotationOptionOld(double l, double w, double h, int r) {
            this.length = l;
            this.width = w;
            this.height = h;
            this.rotation = r;
        }
    }

    private static class Space2D {
        double x;
        double z;
        double length;
        double width;

        Space2D(double x, double z, double length, double width) {
            this.x = x;
            this.z = z;
            this.length = length;
            this.width = width;
        }
    }

    private static class Placement2D {
        Space2D space;
        double rotatedLength;
        double rotatedWidth;
        int rotation;

        Placement2D(Space2D space, double l, double w, int rotation) {
            this.space = space;
            this.rotatedLength = l;
            this.rotatedWidth = w;
            this.rotation = rotation;
        }
    }

    private static class RotationOption2D {
        double length;
        double width;
        int rotation;

        RotationOption2D(double l, double w, int r) {
            this.length = l;
            this.width = w;
            this.rotation = r;
        }
    }

    private static class Space {
        private double x;
        private double y;
        private double z;
        private double length;
        private double width;
        private double height;

        Space(double x, double y, double z, double length, double width, double height) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.length = length;
            this.width = width;
            this.height = height;
        }

        double getX() { return x; }
        double getY() { return y; }
        double getZ() { return z; }
        double getLength() { return length; }
        double getWidth() { return width; }
        double getHeight() { return height; }
        double getVolume() { return length * width * height; }
    }
}
