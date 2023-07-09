package com.example.cookmaster_at_home_app;

import org.json.JSONArray;

import java.util.List;

public interface FidelityItemsCallback {
    void onSuccess(JSONArray items);
    void onError(String errorMessage);

}
