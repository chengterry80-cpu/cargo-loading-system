package com.loading.model;

import java.util.List;

public class LoadingRequest {
    private Truck truck;
    private List<Cargo> cargoList;

    public LoadingRequest() {}

    public LoadingRequest(Truck truck, List<Cargo> cargoList) {
        this.truck = truck;
        this.cargoList = cargoList;
    }

    public Truck getTruck() { return truck; }
    public void setTruck(Truck truck) { this.truck = truck; }
    public List<Cargo> getCargoList() { return cargoList; }
    public void setCargoList(List<Cargo> cargoList) { this.cargoList = cargoList; }
}
