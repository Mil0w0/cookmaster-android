package com.example.cookmaster_at_home_app;

import java.util.List;

public interface LessonClientCallback {
    void onSuccess(int counter, List<Lesson> lessons);
    void onError(String errorMessage);
}
