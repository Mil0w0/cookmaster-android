package com.example.cookmaster_at_home_app;

public class Subscription {
    private final int id;
    private final String name;
    private final Double price;
    private final int maxlessonaccess;

    public Subscription(int id,String name, Double price, int maxlessonaccess) {
        this.name = name;
        this.price = price;
        this.maxlessonaccess = maxlessonaccess;
        this.id = id;
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

    @Override
    public String toString() {
        return "Subscription{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", maxlessonaccess=" + maxlessonaccess +
                '}';
    }

    public int getId() {
        return id;
    }
}
