package com.example.cookmaster_at_home_app;

import java.util.List;

public interface LessonGroupCallback {
    void onSuccess(List<Lesson> lessons);
    void onError(String errorMessage);
}
