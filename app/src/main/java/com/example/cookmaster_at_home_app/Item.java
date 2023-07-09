package com.example.cookmaster_at_home_app;

public class Item {
    private String name, image, description;
    private int id,price, stock, reward;

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
}
