package com.loading.controller;

import com.loading.model.*;
import com.loading.service.LoadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class LoadingController {

    @Autowired
    private LoadingService loadingService;

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @PostMapping("/api/loading/calculate")
    @ResponseBody
    public LoadingResult calculateLoading(@RequestBody LoadingRequest request) {
        return loadingService.calculateLoading(request.getTruck(), request.getCargoList());
    }
    
    @PostMapping("/api/loading/multi-truck")
    @ResponseBody
    public MultiTruckLoadingResult calculateMultiTruckLoading(@RequestBody MultiTruckLoadingRequest request) {
        MultiTruckLoadingResult result = loadingService.calculateMultiTruckLoading(
            request.getTruck(), 
            request.getCargoList(), 
            request.getTruckCount()
        );
        return result;
    }

    @PostMapping("/api/loading/multi-truck/save")
    @ResponseBody
    public LoadingHistory calculateAndSave(@RequestBody MultiTruckLoadingRequest request) {
        MultiTruckLoadingResult result = loadingService.calculateMultiTruckLoading(
            request.getTruck(), 
            request.getCargoList(), 
            request.getTruckCount()
        );
        return loadingService.saveHistory(result, request.getTruck(), request.getCargoList(), request.getTruckCount());
    }

    @GetMapping("/api/history")
    @ResponseBody
    public List<LoadingHistory> getAllHistory() {
        return loadingService.getAllHistory();
    }

    @GetMapping("/api/history/{id}")
    @ResponseBody
    public LoadingHistory getHistoryById(@PathVariable Long id) {
        return loadingService.getHistoryById(id);
    }

    @DeleteMapping("/api/history/{id}")
    @ResponseBody
    public boolean deleteHistory(@PathVariable Long id) {
        return loadingService.deleteHistory(id);
    }

    @GetMapping("/api/export/{id}")
    public ResponseEntity<byte[]> exportHistory(@PathVariable Long id) {
        LoadingHistory history = loadingService.getHistoryById(id);
        if (history == null) {
            return ResponseEntity.notFound().build();
        }

        String content = generateExportContent(history);
        
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(history.getCreateTime());
        String filename = "loading_plan_" + timestamp + ".txt";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/plain;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String generateExportContent(LoadingHistory history) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        sb.append("===== 智能货物装车方案 =====\n\n");
        sb.append("创建时间: ").append(sdf.format(history.getCreateTime())).append("\n\n");
        
        sb.append("===== 车厢参数 =====\n");
        Truck truck = history.getTruck();
        sb.append("长度: ").append(truck.getLength()).append(" cm\n");
        sb.append("宽度: ").append(truck.getWidth()).append(" cm\n");
        sb.append("高度: ").append(truck.getHeight()).append(" cm\n");
        sb.append("最大载重: ").append(truck.getMaxWeight()).append(" kg\n");
        sb.append("车辆数量: ").append(history.getTruckCount()).append("\n\n");
        
        sb.append("===== 货物清单 =====\n");
        for (Cargo cargo : history.getCargoList()) {
            sb.append("- ").append(cargo.getName())
              .append(" | 尺寸: ").append(cargo.getLength()).append("x").append(cargo.getWidth()).append("x").append(cargo.getHeight())
              .append(" cm | 重量: ").append(cargo.getWeight()).append(" kg | 数量: ").append(cargo.getQuantity()).append("\n");
        }
        sb.append("\n");
        
        sb.append("===== 装车结果 =====\n");
        MultiTruckLoadingResult result = history.getResult();
        sb.append(result.getMessage()).append("\n");
        sb.append("使用车辆数: ").append(result.getTotalTrucksUsed()).append("\n\n");
        
        int truckNum = 1;
        for (TruckLoadResult truckResult : result.getTruckResults()) {
            sb.append("--- 第").append(truckNum++).append("辆车 ---\n");
            sb.append("总重量: ").append(truckResult.getTotalWeight()).append(" kg\n");
            sb.append("空间利用率: ").append(String.format("%.1f", truckResult.getUtilizationRate())).append("%\n");
            sb.append("装载件数: ").append(truckResult.getPlacedItems().size()).append("\n");
            
            sb.append("货物位置:\n");
            for (PlacedItem item : truckResult.getPlacedItems()) {
                sb.append("  - ").append(item.getName())
                  .append(" | 位置: (").append(item.getX()).append(",").append(item.getY()).append(",").append(item.getZ()).append(")")
                  .append(" | 尺寸: ").append(item.getLength()).append("x").append(item.getWidth()).append("x").append(item.getHeight())
                  .append(" | 旋转: ").append(item.getRotation()).append("°\n");
            }
            sb.append("\n");
        }
        
        if (result.getUnplacedItems() != null && !result.getUnplacedItems().isEmpty()) {
            sb.append("===== 未装载货物 =====\n");
            for (Cargo cargo : result.getUnplacedItems()) {
                sb.append("- ").append(cargo.getName()).append(" x").append(cargo.getQuantity()).append("\n");
            }
        }
        
        return sb.toString();
    }

    @GetMapping("/api/truck/default")
    @ResponseBody
    public Truck getDefaultTruck() {
        return loadingService.getDefaultTruck();
    }

    @PostMapping("/api/loading/validate")
    @ResponseBody
    public ValidationResult validateCargoList(@RequestBody CargoListRequest request) {
        return loadingService.validateCargoList(request.getCargoList());
    }

    public static class CargoListRequest {
        private List<Cargo> cargoList;
        public List<Cargo> getCargoList() { return cargoList; }
        public void setCargoList(List<Cargo> cargoList) { this.cargoList = cargoList; }
    }
}
