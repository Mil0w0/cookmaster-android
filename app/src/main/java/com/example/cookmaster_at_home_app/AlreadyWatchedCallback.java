package com.example.cookmaster_at_home_app;

import java.util.List;

public interface AlreadyWatchedCallback {
    void onSuccess(boolean iswatched, Lesson lesson);
    void onError(String errorMessage);
}
