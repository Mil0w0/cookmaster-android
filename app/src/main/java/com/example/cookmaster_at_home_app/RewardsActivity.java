package com.example.cookmaster_at_home_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardsActivity extends AppCompatActivity {
    private List<Item> items50;
    private List<Item> items110;
    private List<Item> items180;

    private Button settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        Bundle bundle = getIntent().getExtras();
        int user_id = bundle.getInt("user_id", -1);
        int subscriptionId = bundle.getInt("subscription_id", -1);
        boolean auto_reconnect = bundle.getBoolean("auto_reconnect", false);

        settingsButton = findViewById(R.id.settings_button);
        settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextPage = new Intent(RewardsActivity.this, AccountActivity.class);
                nextPage.putExtra("user_id", user_id);
                nextPage.putExtra("subscription_id", subscriptionId);
                nextPage.putExtra("auto_reconnect", auto_reconnect);
                startActivity(nextPage);
            }
        });

        getItems(new FidelityItemsCallback() {
            @Override
            public void onSuccess(JSONArray items) {
                ArrayList<Item> listItems50 = new ArrayList<>();
                ArrayList<Item> listItems110 = new ArrayList<>();
                ArrayList<Item> listItems180 = new ArrayList<>();
                for (int i = 0; i < items.length(); i++) {
                    try {
                        JSONObject obj = items.getJSONObject(i);
                        int id = obj.getInt("idshopitem");
                        String name = obj.getString("name");
                        String description = obj.getString("description");
                        String image = obj.getString("picture");
                        int price = obj.getInt("price");
                        int reward = obj.getInt("reward");
                        int stock = obj.getInt("stock");

                        switch (reward) {
                            //Replace with the correct values after testing
                            case 50:
                                listItems50.add(new Item(id, name, image,description, price, reward, stock));
                                break;
                            case 110:
                                listItems110.add(new Item(id, name, image,description, price, reward, stock));
                                break;
                            case 180:
                                listItems180.add(new Item(id, name, image,description, price, reward, stock));
                                break;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                items50 = listItems50;
                items110 = listItems110;
                items180 = listItems180;

                RecyclerView recyclerView = findViewById(R.id.rewards50_recycler_view);
                setRecyclerView(items50, recyclerView);

                recyclerView = findViewById(R.id.rewards110_recycler_view);
                setRecyclerView(items110, recyclerView);

                recyclerView = findViewById(R.id.rewards180_recycler_view);
                setRecyclerView(items180, recyclerView);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(RewardsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        RecyclerView recyclerView = findViewById(R.id.rewards50_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

    }
    private void setRecyclerView(List<Item> items, RecyclerView recyclerView){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        RewardsAdapter adapter = new RewardsAdapter(items, RewardsActivity.this);
        recyclerView.setAdapter(adapter);
    }
    public void getItems(FidelityItemsCallback callback){

        RequestQueue rq = Volley.newRequestQueue(RewardsActivity.this);

        String url = "https://api.becomeacookmaster.live:9000/shopitem/all";

        StringRequest query = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {

                            JSONArray json = new JSONArray(response);

                            //SAVE DATA SO WE CAN STILL SEE IT OFFLINE if it is not the same string.
                            SharedPreferences sharedPreferences = getSharedPreferences("shopitems", Context.MODE_PRIVATE);

                            if (!sharedPreferences.getString("shopitems", "null").equals(response)){
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("shopitems", json.toString());
                                editor.apply();
                            }
                            callback.onSuccess(json);

                        }catch (Exception e){
                            Toast.makeText(RewardsActivity.this, String.format(e.toString()) , Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null){
                            String errorMessage = new String(error.networkResponse.data);
                            Toast.makeText(RewardsActivity.this, "ok"+errorMessage, Toast.LENGTH_SHORT).show();
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