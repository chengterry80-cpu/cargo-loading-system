package com.loading.model;

import java.util.ArrayList;
import java.util.List;

public class MultiTruckLoadingResult {
    private boolean success;
    private List<TruckLoadResult> truckResults;
    private List<Cargo> unplacedItems;
    private String message;
    private int totalTrucksUsed;

    public MultiTruckLoadingResult() {
        this.truckResults = new ArrayList<>();
        this.unplacedItems = new ArrayList<>();
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public List<TruckLoadResult> getTruckResults() { return truckResults; }
    public void setTruckResults(List<TruckLoadResult> truckResults) { this.truckResults = truckResults; }
    public List<Cargo> getUnplacedItems() { return unplacedItems; }
    public void setUnplacedItems(List<Cargo> unplacedItems) { this.unplacedItems = unplacedItems; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public int getTotalTrucksUsed() { return totalTrucksUsed; }
    public void setTotalTrucksUsed(int totalTrucksUsed) { this.totalTrucksUsed = totalTrucksUsed; }
}
