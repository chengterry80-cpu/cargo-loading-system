package com.loading.model;

import java.util.Date;
import java.util.List;

public class LoadingHistory {
    private Long id;
    private Date createTime;
    private Truck truck;
    private List<Cargo> cargoList;
    private int truckCount;
    private MultiTruckLoadingResult result;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Truck getTruck() {
        return truck;
    }

    public void setTruck(Truck truck) {
        this.truck = truck;
    }

    public List<Cargo> getCargoList() {
        return cargoList;
    }

    public void setCargoList(List<Cargo> cargoList) {
        this.cargoList = cargoList;
    }

    public int getTruckCount() {
        return truckCount;
    }

    public void setTruckCount(int truckCount) {
        this.truckCount = truckCount;
    }

    public MultiTruckLoadingResult getResult() {
        return result;
    }

    public void setResult(MultiTruckLoadingResult result) {
        this.result = result;
    }
}
