package com.example.cookmaster_at_home_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class LessonActivity extends AppCompatActivity {

    private TextView lesson_name;
    private TextView lesson_description;
    private TextView lesson_author;
    private TextView lesson_difficulty;
    private ImageView lesson_image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        Bundle lesson = getIntent().getExtras();
        String title = lesson.getString("name");
        String description = lesson.getString("description");
        String content = lesson.getString("content");
        String author = lesson.getString("author");
        int difficulty = lesson.getInt("difficulty");


        lesson_name = findViewById(R.id.lesson_title);
        lesson_description = findViewById(R.id.lesson_description);
        lesson_author = findViewById(R.id.lesson_author);
        lesson_difficulty = findViewById(R.id.lesson_difficulty);

        lesson_name.setText(title);
        lesson_description.setText("Description: "+ description);
        lesson_author.setText("by " + author);
        lesson_difficulty.setText(Integer.toString(difficulty));
    }
}