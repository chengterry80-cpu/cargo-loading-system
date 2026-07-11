package com.loading.model;

import java.util.ArrayList;
import java.util.List;

public class LoadingResult {
    private boolean success;
    private double totalWeight;
    private double totalVolume;
    private double utilizationRate;
    private List<PlacedItem> placedItems;
    private List<Cargo> unplacedItems;
    private String message;

    public LoadingResult() {
        this.placedItems = new ArrayList<>();
        this.unplacedItems = new ArrayList<>();
    }

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
    public List<Cargo> getUnplacedItems() { return unplacedItems; }
    public void setUnplacedItems(List<Cargo> unplacedItems) { this.unplacedItems = unplacedItems; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
