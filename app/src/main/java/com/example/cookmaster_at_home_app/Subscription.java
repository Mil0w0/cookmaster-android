package com.example.cookmaster_at_home_app;

public class Subscription {
    private String name;
    private Double price;
    private int maxlessonaccess;

    public Subscription(String name, Double price, int maxlessonaccess) {
        this.name = name;
        this.price = price;
        this.maxlessonaccess = maxlessonaccess;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public int getMaxlessonaccess() {
        return maxlessonaccess;
    }
}
