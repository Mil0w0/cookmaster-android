package com.example.cookmaster_at_home_app;

public interface ItemCallback {
    void onSuccess(String name, String image, String description, int price, int stock, int reward);
    void onError(String message);
}
