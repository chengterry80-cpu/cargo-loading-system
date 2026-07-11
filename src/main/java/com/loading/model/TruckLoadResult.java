package com.loading.model;

import java.util.ArrayList;
import java.util.List;

public class TruckLoadResult {
    private int truckNumber;
    private boolean success;
    private double totalWeight;
    private double totalVolume;
    private double utilizationRate;
    private List<PlacedItem> placedItems;
    private Truck truckSpec;

    public TruckLoadResult() {
        this.placedItems = new ArrayList<>();
    }

    public int getTruckNumber() { return truckNumber; }
    public void setTruckNumber(int truckNumber) { this.truckNumber = truckNumber; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public double getTotalWeight() { return totalWeight; }
    public void setTotalWeight(double totalWeight) { this.totalWeight = totalWeight; }
    public double getTotalVolume() { return totalVolume; }
    public void setTotalVolume(double totalVolume) { this.totalVolume = totalVolume; }
    public double getUtilizationRate() { return utilizationRate; }
    public void setUtilizationRate(double utilizationRate) { this.utilizationRate = utilizationRate; }
    public List<PlacedItem> getPlacedItems() { return placedItems; }
    public void setPlacedItems(List<PlacedItem> placedItems) { this.placedItems = placedItems; }
    public Truck getTruckSpec() { return truckSpec; }
    public void setTruckSpec(Truck truckSpec) { this.truckSpec = truckSpec; }
}
