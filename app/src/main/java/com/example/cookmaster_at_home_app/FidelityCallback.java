package com.example.cookmaster_at_home_app;

public interface FidelityCallback {
    void onSuccess(int id, String firstname,String lastname, String email, int fidelity_points);
    void onError(String errorMessage);
}
