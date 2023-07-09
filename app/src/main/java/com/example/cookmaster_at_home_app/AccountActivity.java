package com.example.cookmaster_at_home_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {
    TextView nom_text ;
    TextView email_text ;
    TextView client_subscription_name;
    TextView client_subscription_maxlessonaccess;
    Button btn_lessons;
    Button btn_fidelity;
    Button log_out;
    Button retry;
    CheckBox auto_reconnect_checkbox;
    LinearLayout layout;

    private boolean auto_reconnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        layout = findViewById(R.id.account_info);


        btn_lessons = findViewById(R.id.lessons_button);
        log_out = findViewById(R.id.logout_button);
        btn_fidelity = findViewById(R.id.fidelity_button);
        auto_reconnect_checkbox = findViewById(R.id.auto_reconnect);

        if (!NetworkHelper.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
            btn_lessons.setVisibility(View.GONE);
            auto_reconnect_checkbox.setVisibility(View.GONE);
            btn_fidelity.setVisibility(View.GONE);
            log_out.setVisibility(View.GONE);

            //make a new button
            retry = new Button(AccountActivity.this);
            retry.setBackgroundColor(getColor(R.color.brand_primary_color));
            retry.setText("Retry");
            retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recreate();
                }
            });
            layout.addView(retry);
            return;
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int user_id = extras.getInt("user_id");
            String clientEmail = extras.getString("email");
            String clientSubscriptionName = extras.getString("subscription_name");
            int clientSubscriptionMaxLessons = extras.getInt("subscription_maxlessonaccess");
            int clientSubscriptionId = extras.getInt("subscription_id");
            auto_reconnect = extras.getBoolean("auto_reconnect");

            if (clientSubscriptionName == null || clientEmail == null || clientSubscriptionMaxLessons == 0) {
                if (user_id == 0) {
                    Toast.makeText(AccountActivity.this, "Can't get user info, missing user id.", Toast.LENGTH_SHORT).show();
                    Intent nextPage = new Intent(AccountActivity.this, LoginActivity.class);
                    startActivity(nextPage);
                } else {
                    getUserInfo(user_id, clientSubscriptionId, new GetUserByIDCallback() {
                        @Override
                        public void onSuccess(Client client) {

                            nom_text = findViewById(R.id.client_name);
                            nom_text.setText("Name : " + client.getFullName());

                            email_text = findViewById(R.id.client_email);
                            email_text.setText("Email : " + client.getEmail());

                            auto_reconnect_checkbox.setChecked(auto_reconnect);

                            client_subscription_name = findViewById(R.id.client_subscription_name);
                            client_subscription_name.setText("Subscription : " + client.getSubscription().getName());

                            client_subscription_maxlessonaccess = findViewById(R.id.client_subscription_maxlessonaccess);
                            client_subscription_maxlessonaccess.setText("Max Lesson Access : " + Integer.toString(client.getSubscription().getMaxlessonaccess()));


                            auto_reconnect_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                                    auto_reconnect = isChecked;
                                    SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    if (auto_reconnect) {
                                        editor.putInt("user_id", user_id);
                                        editor.putInt("subscription_id", client.getSubscription().getId());
                                    }else{
                                        editor.clear();
                                    }
                                    editor.putBoolean("auto_reconnect", auto_reconnect);
                                    editor.apply();
                                }
                            });


                            btn_lessons.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent nextPage = new Intent(AccountActivity.this, LessonsActivity.class);
                                    nextPage.putExtra("user_id", user_id);
                                    nextPage.putExtra("fullname", client.getFullName());
                                    nextPage.putExtra("email", client.getEmail());
                                    nextPage.putExtra("subscription_name", client.getSubscription().getName());
                                    nextPage.putExtra("subscription_id", client.getSubscription().getId());
                                    nextPage.putExtra("subscription_maxlessonaccess", client.getSubscription().getMaxlessonaccess());
                                    nextPage.putExtra("auto_reconnect", auto_reconnect);
                                    startActivity(nextPage);
                                }
                            });
                            btn_fidelity.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent otherpage = new Intent(AccountActivity.this, FidelityOverviewActivity.class);
                                    otherpage.putExtra("user_id", user_id);
                                    otherpage.putExtra("subscription_id", client.getSubscription().getId());
                                    otherpage.putExtra("auto_reconnect", auto_reconnect);
                                    startActivity(otherpage);
                                }
                            });
                            log_out.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //logout and remove user_id from shared preferences
                                    SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.clear();
                                    editor.apply();

                                    Intent otherpage = new Intent(AccountActivity.this, LoginActivity.class);
                                    startActivity(otherpage);
                                }
                            });
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Toast.makeText(AccountActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            Intent nextPage = new Intent(AccountActivity.this, LoginActivity.class);
                            startActivity(nextPage);
                        }
                    });
                }
            } else {

//                nom_text = findViewById(R.id.client_name);
//                nom_text.setText("Name : " + clientFullname);

                email_text = findViewById(R.id.client_email);
                email_text.setText("Email : " + clientEmail);

                auto_reconnect_checkbox = findViewById(R.id.auto_reconnect);
                auto_reconnect_checkbox.setChecked(auto_reconnect);

                client_subscription_name = findViewById(R.id.client_subscription_name);
                client_subscription_name.setText("Subscription : " + clientSubscriptionName);

                client_subscription_maxlessonaccess = findViewById(R.id.client_subscription_maxlessonaccess);
                client_subscription_maxlessonaccess.setText("Max Lesson Access : " + Integer.toString(clientSubscriptionMaxLessons));

                log_out = findViewById(R.id.logout_button);

                btn_lessons = findViewById(R.id.lessons_button);
                btn_fidelity = findViewById(R.id.fidelity_button);

                auto_reconnect_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        auto_reconnect = isChecked;
                        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("auto_reconnect", auto_reconnect);
                        editor.putInt("user_id", user_id);
                        editor.putInt("subscription_id", clientSubscriptionId);
                        editor.apply();
                    }
                });


                btn_lessons.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent nextPage = new Intent(AccountActivity.this, LessonsActivity.class);
                        nextPage.putExtra("user_id", user_id);
                        nextPage.putExtra("email", clientEmail);
                        nextPage.putExtra("subscription_name", clientSubscriptionName);
                        nextPage.putExtra("subscription_maxlessonaccess", clientSubscriptionMaxLessons);
                        startActivity(nextPage);
                    }
                });
                btn_fidelity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent otherpage = new Intent(AccountActivity.this, FidelityOverviewActivity.class);
                        otherpage.putExtra("user_id", user_id);
                        otherpage.putExtra("subscription_id", clientSubscriptionId);
                        otherpage.putExtra("auto_reconnect", auto_reconnect);
                        startActivity(otherpage);
                    }
                });
                log_out.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //logout and remove user_id from shared preferences
                        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();
                        Toast.makeText(AccountActivity.this, "Come back soon !", Toast.LENGTH_SHORT).show();
                        Intent otherpage = new Intent(AccountActivity.this, LoginActivity.class);
                        startActivity(otherpage);
                    }
                });
            }
        }
    }

    private void getUserInfo(int user_id, int clientSubscriptionId, GetUserByIDCallback callback){

        RequestQueue rq = Volley.newRequestQueue(AccountActivity.this);
        String url = "https://api.becomeacookmaster.live:9000/user/" + Integer.toString(user_id);

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
                            } else {
                                String lastname = json_response.getString("lastname");
                                String firstname = json_response.getString("firstname");
                                String email = json_response.getString("email");
                                String profile_picture = json_response.getString("profilepicture");
                                int language = json_response.getInt("language");
                                String isblocked = json_response.getString("isblocked");

                                SubscriptionCallback sub_callback = new SubscriptionCallback(){
                                    @Override
                                    public void onSuccess(Subscription subscription) {
                                        Client client = new Client(isblocked, user_id, email, language, profile_picture, firstname, lastname, subscription);
                                        callback.onSuccess(client);
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        Toast.makeText(AccountActivity.this, errorMessage + "found it !", Toast.LENGTH_SHORT).show();
                                    }
                                };
                                getSubscriptionInfo(rq, clientSubscriptionId , sub_callback);


                            }
                        }catch (Exception e){
                            Toast.makeText(AccountActivity.this,"ERROR 2: %s".format(e.toString()) , Toast.LENGTH_SHORT).show();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("%s".format(error.toString()));
                    }
                }
        ){
            @Override
            //ADD HEADERS TO REQUEST
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("Token", getResources().getString(R.string.tokenAPI));
                return params;
            }
        };
        rq.add(query);
    }

    private void getSubscriptionInfo(RequestQueue rq, int subscriptionId, SubscriptionCallback sub_callback) {
        String url = "https://api.becomeacookmaster.live:9000/subscription/" + Integer.toString(subscriptionId);
        StringRequest query = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject json_response = new JSONObject(response);
                            if (json_response.has("error"))
                            {
                                String error = json_response.getString("message");
                                int error_code = json_response.getInt("error");
                            }
                            else
                            {
                                String name = json_response.getString("name");
                                Double price = json_response.getDouble("price");
                                int maxlessonaccess = json_response.getInt("maxlessonaccess");
                                Subscription subscription = new Subscription(subscriptionId,name, price, maxlessonaccess);
                                sub_callback.onSuccess(subscription);
                            }
                        } catch (Exception e){
                            sub_callback.onError(e.toString() + "ERROR làT");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        sub_callback.onError(error.toString() + "ERROR ici");
                    }
                }
        ){
            @Override
            //ADD HEADERS TO REQUEST
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("Token", getResources().getString(R.string.tokenAPI));
                return params;
            }
        };
        rq.add(query);
    }
}
