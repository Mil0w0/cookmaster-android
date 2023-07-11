package com.example.cookmaster_at_home_app;

public class Item {
    private final String name;
    private final String image;
    private final String description;
    private final int id;
    private final int price;
    private final int stock;
    private final int reward;

    public Item(int id, String name, String image, String description, int price, int stock, int reward) {
        this.name = name;
        this.image = image;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.reward = reward;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public int getReward() {
        return reward;
    }

    public int getId() {
        return id;
    }
}
