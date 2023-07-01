package com.example.cookmaster_at_home_app;

public interface SubscriptionCallback {
    void onSuccess(Subscription subscription);
    void onError(String errorMessage);
}
