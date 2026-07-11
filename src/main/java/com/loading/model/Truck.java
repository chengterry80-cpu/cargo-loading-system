package com.loading.model;

public class Truck {
    private double length;
    private double width;
    private double height;
    private double maxWeight;

    public Truck() {}

    public Truck(double length, double width, double height, double maxWeight) {
        this.length = length;
        this.width = width;
        this.height = height;
        this.maxWeight = maxWeight;
    }

    public double getLength() { return length; }
    public void setLength(double length) { this.length = length; }
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    public double getMaxWeight() { return maxWeight; }
    public void setMaxWeight(double maxWeight) { this.maxWeight = maxWeight; }

    public double getVolume() {
        return length * width * height;
    }
}
