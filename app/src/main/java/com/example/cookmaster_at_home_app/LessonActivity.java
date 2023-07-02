package com.example.cookmaster_at_home_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class LessonActivity extends AppCompatActivity {

    private TextView lesson_name;
    private TextView lesson_description;
    private TextView lesson_author;
    private TextView lesson_content;
    private Button back_button;

    private ListView lessonGroupListView;
    private List<Lesson> GroupLessons;

    private ImageView lesson_image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        Bundle extras = getIntent().getExtras();
        String title = extras.getString("name");
        int idlesson = extras.getInt("idlesson");
        String description = extras.getString("description");
        String content = extras.getString("content");
        String author = extras.getString("author");
        String image = extras.getString("picture");
        int difficulty = extras.getInt("difficulty");
        String clientFullname = extras.getString("fullname");
        int groupId = extras.getInt("group_id");
        int clientId = extras.getInt("user_id");
        String clientEmail = extras.getString("email");
        String clientSubscriptionName = extras.getString("subscription_name");
        int clientSubscriptionMaxLessons = extras.getInt("subscription_maxlessonaccess");

        Lesson lesson = new Lesson(title, idlesson, description, image, difficulty, content, author, groupId);

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

        //DISPLAY ALL LESSONS FROM THE GROUP
        lessonGroupListView = findViewById(R.id.listGroupLessons);
        LessonAdapter lesson_adapter = new LessonAdapter(GroupLessons,LessonActivity.this);
        lessonGroupListView.setAdapter(lesson_adapter);


        back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextPage = new Intent(LessonActivity.this, LessonsActivity.class);
                nextPage.putExtra("fullname", clientFullname);
                nextPage.putExtra("user_id", clientId);
                nextPage.putExtra("email", clientEmail);
                nextPage.putExtra("subscription_name", clientSubscriptionName);
                nextPage.putExtra("subscription_maxlessonaccess", clientSubscriptionMaxLessons);
                //group/image/ytb to add?
                startActivity(nextPage);
            }
        });
    }
}