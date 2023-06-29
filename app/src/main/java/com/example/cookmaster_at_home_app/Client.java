package com.example.cookmaster_at_home_app;

public class Client {
    private int id;
    private String name;
    private int subscription;

    public Client(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSubscription() {
        return subscription;
    }
}
