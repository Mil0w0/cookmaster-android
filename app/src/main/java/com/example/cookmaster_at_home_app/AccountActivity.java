package com.example.cookmaster_at_home_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class AccountActivity extends AppCompatActivity {
    TextView nom_text ;
    TextView email_text ;
    TextView client_subscription_name;
    TextView client_subscription_maxlessonaccess;
    Button btn_lessons;
    Button btn_fidelity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int user_id = extras.getInt("user_id");
            String clientFullname = extras.getString("fullname");
            String clientEmail = extras.getString("email");
            String clientSubscriptionName = extras.getString("subscription_name");
            int clientSubscriptionMaxLessons = extras.getInt("subscription_maxlessonaccess");

            nom_text = findViewById(R.id.client_name);
            nom_text.setText("Name : " + clientFullname);

            email_text = findViewById(R.id.client_email);
            email_text.setText("Email : " + clientEmail);


            client_subscription_name = findViewById(R.id.client_subscription_name);
            client_subscription_name.setText("Subscription : " + clientSubscriptionName);

            client_subscription_maxlessonaccess = findViewById(R.id.client_subscription_maxlessonaccess);
            client_subscription_maxlessonaccess.setText("Max Lesson Access : " + Integer.toString(clientSubscriptionMaxLessons));


            btn_lessons = findViewById(R.id.lessons_button);
            btn_fidelity = findViewById(R.id.fidelity_button);

            btn_lessons.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent nextPage = new Intent(AccountActivity.this, LessonsActivity.class);
                    nextPage.putExtra("user_id", user_id);
                    nextPage.putExtra("fullname", clientFullname);
                    nextPage.putExtra("email",clientEmail);
                    nextPage.putExtra("subscription_name", clientSubscriptionName);
                    nextPage.putExtra("subscription_maxlessonaccess", clientSubscriptionMaxLessons);
                    startActivity(nextPage);
                }
            });
            btn_fidelity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: change intent when created
                    Intent otherpage = new Intent(AccountActivity.this, LessonsActivity.class);
                    startActivity(otherpage);
                }
            });
        }
    }
}