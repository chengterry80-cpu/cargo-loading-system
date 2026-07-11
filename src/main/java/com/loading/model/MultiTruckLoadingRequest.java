package com.loading.model;

import java.util.List;

public class MultiTruckLoadingRequest {
    private Truck truck;
    private List<Cargo> cargoList;
    private int truckCount;

    public MultiTruckLoadingRequest() {}

    public Truck getTruck() { return truck; }
    public void setTruck(Truck truck) { this.truck = truck; }
    public List<Cargo> getCargoList() { return cargoList; }
    public void setCargoList(List<Cargo> cargoList) { this.cargoList = cargoList; }
    public int getTruckCount() { return truckCount; }
    public void setTruckCount(int truckCount) { this.truckCount = truckCount; }
}
