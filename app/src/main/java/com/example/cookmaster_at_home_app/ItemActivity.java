package com.example.cookmaster_at_home_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ItemActivity extends AppCompatActivity {
    private TextView item_name, item_description, item_reward;
    private Button item_btn, settingsButton;
    private ImageView item_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        Bundle bundle = getIntent().getExtras();
        int item_id = bundle.getInt("item_id", -1);
        int user_id = bundle.getInt("user_id", -1);
        int subscriptionId = bundle.getInt("subscription_id", -1);
        boolean auto_reconnect = bundle.getBoolean("auto_reconnect", false);
        int fidelity_points = bundle.getInt("fidelitypoints", -1);

        if (item_id == -1) {
            Toast.makeText(ItemActivity.this, "Item not found", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ItemActivity.this, RewardsActivity.class);
            startActivity(intent);
        }

        settingsButton = findViewById(R.id.settings_button);
        settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextPage = new Intent(ItemActivity.this, AccountActivity.class);
                nextPage.putExtra("user_id", user_id);
                nextPage.putExtra("subscription_id", subscriptionId);
                nextPage.putExtra("auto_reconnect", auto_reconnect);
                startActivity(nextPage);
            }
        });

        getItem(item_id, new ItemCallback() {
            @Override
            public void onSuccess(String name, String description, String image, int price, int reward, int stock) {

                Item item = new Item(item_id, name, image, description, price, stock, reward);

                item_name = findViewById(R.id.item_title);
                item_description = findViewById(R.id.item_description);
                item_reward = findViewById(R.id.item_reward);
                item_btn = findViewById(R.id.item_button);
                item_image = findViewById(R.id.item_image);

                item_name.setText(item.getName());
                item_description.setText("Description :\n" + item.getDescription());

                if (fidelity_points < item.getReward())
                    item_reward.setText("You don't have enough fidelity points to get this item.");
                else
                    item_reward.setText("Choosing this item, will remove " + item.getReward() + " fidelity points from your account.");

                String url = "https://becomeacookmaster.live/assets/images/shop-items/" + item.getImage();

                Picasso.get().load(url).into(item_image);

                item_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (fidelity_points >= item.getReward()) {
                           if (item.getStock() > 0) {

                              decreaseFidelityPoints(user_id, (fidelity_points - item.getReward()), new FidelityPointsCallback() {
                                  @Override
                                  public void onSuccess(String message) {
                                      updateItemStock(item_id, item.getStock() - 1, new UpdateStockCallback(){
                                            @Override
                                            public void onSuccess(String message) {
                                                sendMail(user_id, item);
                                            }

                                            @Override
                                            public void onError(String message) {
                                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                            }
                                      });
                                  }

                                  @Override
                                  public void onError(String message) {
                                      Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                  }
                              });
                           }
                        }
                    }
                });
            }

            public void onError(String message) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendMail(int user_id, Item item) {
        Toast.makeText(getApplicationContext(), "Item reserved. You will receive a mail shortly", Toast.LENGTH_SHORT).show();
    }

    private void updateItemStock(int item_id, int new_stock, UpdateStockCallback updateStockCallback) {
        Map<String, Integer> params = new HashMap<String, Integer>();
        //ADD PARAMS HERE
        params.put("stock", new_stock);

        RequestQueue rq = Volley.newRequestQueue(ItemActivity.this);

        String url = "https://api.becomeacookmaster.live:9000/shopitem/" + item_id;

        JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.PATCH ,url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                       JSONObject jsonObject = response;
                       updateStockCallback.onSuccess(jsonObject.optString("message"));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ItemActivity.this, String.format(error.toString()), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            //ADD HEADERS TO REQUEST
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("Token", getResources().getString(R.string.tokenAPI));
                return params;
            }
        };
        rq.add(request_json);
    }

    private void decreaseFidelityPoints(int user_id, int reward, FidelityPointsCallback fidelityPointsCallback) {
        Map<String, Integer> params = new HashMap<String, Integer>();
        //ADD PARAMS HERE
        params.put("fidelitypoints",reward);

        RequestQueue rq = Volley.newRequestQueue(ItemActivity.this);

        String url = "https://api.becomeacookmaster.live:9000/client/" + user_id;

        JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.PATCH ,url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject jsonObject = response;
                        String message = jsonObject.optString("message");
                        fidelityPointsCallback.onSuccess(message);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ItemActivity.this, String.format(error.toString()), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            //ADD HEADERS TO REQUEST
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("Token", getResources().getString(R.string.tokenAPI));
                return params;
            }
        };
        rq.add(request_json);
    }

    @Override
    public void onBackPressed() {
        Bundle bundle = getIntent().getExtras();
        int user_id = bundle.getInt("user_id", -1);
        int subscriptionId = bundle.getInt("subscription_id", -1);
        boolean auto_reconnect = bundle.getBoolean("auto_reconnect", false);

        Intent intent = new Intent(ItemActivity.this, FidelityOverviewActivity.class);
        intent.putExtra("user_id", user_id);
        intent.putExtra("subscription_id", subscriptionId);
        intent.putExtra("auto_reconnect", auto_reconnect);
        startActivity(intent);

        finish();
    }

    private void getItem(int item_id, ItemCallback item_reserved) {

        RequestQueue rq = Volley.newRequestQueue(ItemActivity.this);

        String url = "https://api.becomeacookmaster.live:9000/shopitem/"+ item_id;

        StringRequest query = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject json = new JSONObject(response);

                            String name = json.getString("name");
                            String description = json.getString("description");
                            String image = json.getString("picture");
                            int price = json.getInt("price");
                            int reward = json.getInt("reward");
                            int stock = json.getInt("stock");

                            item_reserved.onSuccess(name, description, image, price, reward, stock);

                        }catch (Exception e){
                            Toast.makeText(ItemActivity.this, String.format(e.toString()) , Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null){
                            String errorMessage = new String(error.networkResponse.data);
                            Toast.makeText(ItemActivity.this, "ok"+errorMessage, Toast.LENGTH_SHORT).show();
                        }
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