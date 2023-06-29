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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class LoginActivity extends AppCompatActivity {
    private TextView signUp, forgotPassword;
    private String url;
    private Button login_btn;

    private EditText input_password;
    private EditText input_login;
    private String hashed_password;

    private Client client;
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
        String email = input_login.getText().toString();

        Map<String, String> params = new HashMap<String, String>();
        //ADD PARAMS HERE
        params.put("email", email);

        RequestQueue rq = Volley.newRequestQueue(LoginActivity.this);

        String url = "http://api.becomeacookmaster.live:9000/user/password";

        JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST ,url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            hashed_password = response.getString("password");

                            BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), (CharSequence) hashed_password);

                            if (result.verified) {
                                input_password.setText("");
                                params.put("password", hashed_password);
                                login(rq, (HashMap) params);
                            } else {
                                Toast.makeText(LoginActivity.this, "WRONG PASSWORD", Toast.LENGTH_SHORT).show();
                            }
                       } catch (Exception e) {
                            Toast.makeText(LoginActivity.this, "ERROR: %s".format(e.toString()), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, "ERROR: %s".format(error.toString()), Toast.LENGTH_SHORT).show();
            }
        }){
        @Override
            //ADD HEADERS TO REQUEST
            public Map<String, String> getHeaders() throws AuthFailureError {

            Map<String, String> params = new HashMap<String, String>();
            params.put("Token", "TTGMJe1gEaCGgcq5qtHxoUyulzIvkKhBloPP9HwOey3gpDeZnGeYBKCGbJUd");
            return params;
        }
        };
        rq.add(request_json);

    }

    private void login( RequestQueue rq, HashMap params) {

        String url = "http://api.becomeacookmaster.live:9000/user/login";
        JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST , url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("id")){
                                int id = response.getInt("id");
                                client = new Client(id) ;
                                params.put("id", id);
                                params.remove("email");
                                params.remove("password");
                                Toast.makeText(LoginActivity.this, "", Toast.LENGTH_SHORT).show();
//                                setClientInfo(rq, client, params);
                                Intent nextPage = new Intent(LoginActivity.this, LessonsActivity.class);
                                startActivity(nextPage);
                            } else {
//                                nique ta mère.chais po
                            }

                        } catch (Exception e) {
                            Toast.makeText(LoginActivity.this, "ERROR: %s".format(e.toString()), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, "ERROR: %s".format(error.toString()), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            //ADD HEADERS TO REQUEST
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("Token", "TTGMJe1gEaCGgcq5qtHxoUyulzIvkKhBloPP9HwOey3gpDeZnGeYBKCGbJUd");
                return params;
            }
        };
        rq.add(request_json);
    }
}