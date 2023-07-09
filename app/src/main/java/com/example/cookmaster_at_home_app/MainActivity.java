package com.example.cookmaster_at_home_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
  private ImageButton logo;
  private boolean auto_reconnect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logo = findViewById(R.id.imageButton);

        //get shared preferences auto_reconnection
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        auto_reconnect = sharedPreferences.getBoolean("auto_reconnect", false);
        int user_id = sharedPreferences.getInt("user_id", -1);
        int subscription_id = sharedPreferences.getInt("subscription_id", -1);

        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkHelper.isNetworkAvailable(MainActivity.this)) {

                    if (auto_reconnect) {
                        //go to account activity
                        Intent nextPage = new Intent(MainActivity.this, AccountActivity.class);
                        nextPage.putExtra("auto_reconnect", auto_reconnect);
                        nextPage.putExtra("user_id", user_id);
                        nextPage.putExtra("subscription_id", subscription_id);
                        startActivity(nextPage);
                    } else {
                        //go to login activity
                        Intent nextPage = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(nextPage);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No internet connection. We can't log you in.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}