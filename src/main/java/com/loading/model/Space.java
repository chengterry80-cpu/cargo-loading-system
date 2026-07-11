package com.loading.model;

public class Space {
    private double x;
    private double y;
    private double z;
    private double length;
    private double width;
    private double height;

    public Space() {}

    public Space(double x, double y, double z, double length, double width, double height) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.length = length;
        this.width = width;
        this.height = height;
    }

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

    public double getVolume() {
        return length * width * height;
    }
}
