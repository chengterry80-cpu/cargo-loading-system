package com.loading.model;

public class PlacedItem {
    private Long cargoId;
    private String name;
    private double x;
    private double y;
    private double z;
    private double length;
    private double width;
    private double height;
    private int rotation;

    public PlacedItem() {}

    public PlacedItem(Long cargoId, String name, double x, double y, double z, double length, double width, double height, int rotation) {
        this.cargoId = cargoId;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.length = length;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
    }

    public Long getCargoId() { return cargoId; }
    public void setCargoId(Long cargoId) { this.cargoId = cargoId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }
    public double getLength() { return length; }
    public void setLength(double length) { this.length = length; }
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    public int getRotation() { return rotation; }
    public void setRotation(int rotation) { this.rotation = rotation; }

    public double getVolume() {
        return length * width * height;
    }
}
