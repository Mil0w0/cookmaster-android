package com.example.cookmaster_at_home_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LessonActivity extends AppCompatActivity {

    private TextView lesson_name;
    private TextView lesson_description;
    private TextView lesson_author;
    private TextView lesson_content;


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
        lesson_content = findViewById(R.id.lesson_content);

        LinearLayout starsLayout = findViewById(R.id.difficulty_stars);

        int starSizeInPixels = getResources().getDimensionPixelSize(R.dimen.star_size);

        for (int i = 0; i < difficulty; i++) {
            ImageView starImageView = new ImageView(this);
            starImageView.setImageResource(R.drawable.star_icon_blue);
            starImageView.setLayoutParams(new LinearLayout.LayoutParams(starSizeInPixels, starSizeInPixels));
            starsLayout.addView(starImageView);
        }

        lesson_name.setText(title);
        lesson_description.setText("Description: "+ description);
        lesson_author.setText("by " + author);
        lesson_content.setText(content);
    }
}