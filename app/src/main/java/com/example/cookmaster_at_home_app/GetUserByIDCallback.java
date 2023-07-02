package com.example.cookmaster_at_home_app;

import java.util.List;

public interface GetUserByIDCallback {
    void onSuccess(Client client);
    void onError(String errorMessage);
}
