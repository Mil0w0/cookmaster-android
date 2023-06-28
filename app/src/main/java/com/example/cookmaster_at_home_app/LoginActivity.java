package com.example.cookmaster_at_home_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class LoginActivity extends AppCompatActivity {
    private TextView signUp, forgotPassword;
    private String url;
    private Button login_btn;

    private EditText input_password;
    private EditText input_login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        signUp = findViewById(R.id.create_account);
        forgotPassword = findViewById(R.id.forgot_password);

        login_btn = findViewById(R.id.connect_button);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                url = "https://becomeacookmaster.live/users/signUp";
                Intent nextPage = new Intent(Intent.ACTION_VIEW);
                nextPage.setData(Uri.parse(url));
                startActivity(nextPage);
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                url = "https://becomeacookmaster.live/";
                Intent nextPage = new Intent(Intent.ACTION_VIEW);
                nextPage.setData(Uri.parse(url));
                startActivity(nextPage);
            }
        });
    }

    private void signIn() {

        //GET VALUES FROM INPUTS
        input_password = findViewById(R.id.password_input) ;
        input_login = findViewById(R.id.login_input);;
        String password = input_password.getText().toString();
        String login = input_login.getText().toString();

        //BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword);


        RequestQueue rq = Volley.newRequestQueue(LoginActivity.this);

        String url = "http://api.becomeacookmaster.live:9000";

        StringRequest query = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        Toast.makeText(LoginActivity.this, response, Toast.LENGTH_SHORT).show();

                        try {
                            JSONObject json = new JSONObject(response);
                            String message = json.getString("message");
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(LoginActivity.this, "ERROR: %s".format(e.toString()), Toast.LENGTH_SHORT).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, "ERROR: %s".format(error.toString()), Toast.LENGTH_SHORT).show();

                    }
                }
        );
        rq.add(query);
    }
}