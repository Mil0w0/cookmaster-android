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
import com.android.volley.ServerError;
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
                            Toast.makeText(LoginActivity.this, "ERROR 4: %s".format(e.toString()), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, "ERROR 3: %s".format(error.toString()), Toast.LENGTH_SHORT).show();
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

    private void login(RequestQueue rq, HashMap params) {

        String url = "http://api.becomeacookmaster.live:9000/user/login";
        JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST , url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                            if (response.has("id") && response.has("isblocked") && response.has("subscription") && response.has("role")){
                                try {
                                    int id = response.getInt("id");
                                    String isblocked = response.getString("isblocked");
                                    int subscription = response.getInt("subscription");
                                    String role = response.getString("role");

                                    client = new Client(id, isblocked, subscription, role, params.get("email").toString());

                                    setClientInfo(rq, client);

                                    Intent nextPage = new Intent(LoginActivity.this, AccountActivity.class);
                                        nextPage.putExtra("fullname", client.getFullName());
                                        nextPage.putExtra("email", client.getEmail());
                                    startActivity(nextPage);
                                }catch (JSONException je) {
                                    Toast.makeText(LoginActivity.this, "ERROR json: %s".format(je.getMessage()), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                if (response.has("error")){
                                    try {
                                        String error = response.getString("message");
                                        int error_code = response.getInt("error");
                                        Toast.makeText(LoginActivity.this, Integer.toString(error_code) +": "+ error, Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        Toast.makeText(LoginActivity.this, "ERROR 2: %s".format(e.getMessage()),Toast.LENGTH_SHORT).show();
                                    }
                                  }
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

    private void setClientInfo(RequestQueue rq, Client client) {
    //GetUserByID

        String url = "http://api.becomeacookmaster.live:9000/user/" + client.getId();

        StringRequest query = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json_response = new JSONObject(response);
                            if (json_response.has("error")){
                                String error = json_response.getString("message");
                                int error_code = json_response.getInt("error");
                                Toast.makeText(LoginActivity.this, Integer.toString(error_code) +": "+ error, Toast.LENGTH_SHORT).show();
                            } else {
                                String lastname = json_response.getString("lastname");
                                String firstname = json_response.getString("firstname");
                                int language = json_response.getInt("language");

                                client.setLastname(lastname);
                                client.setFirstname(firstname);
                                client.setLanguage(language);
                            }
                        }catch (Exception e){
                            Toast.makeText(LoginActivity.this,"ERROR 2: %s".format(e.toString()) , Toast.LENGTH_SHORT).show();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this,"ERROR: %s".format(error.toString()) , Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            //ADD HEADERS TO REQUEST
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("Token", "TTGMJe1gEaCGgcq5qtHxoUyulzIvkKhBloPP9HwOey3gpDeZnGeYBKCGbJUd");
                return params;
            }
        };
        rq.add(query);
    }
}