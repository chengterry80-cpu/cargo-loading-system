package com.loading.model;

public class Cargo {
    private Long id;
    private String name;
    private double length;
    private double width;
    private double height;
    private double weight;
    private int quantity;

    public Cargo() {}

    public Cargo(Long id, String name, double length, double width, double height, double weight, int quantity) {
        this.id = id;
        this.name = name;
        this.length = length;
        this.width = width;
        this.height = height;
        this.weight = weight;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getLength() { return length; }
    public void setLength(double length) { this.length = length; }
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getVolume() {
        return length * width * height;
    }
}
