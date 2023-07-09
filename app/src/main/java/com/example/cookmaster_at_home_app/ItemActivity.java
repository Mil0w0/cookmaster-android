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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ItemActivity extends AppCompatActivity {
    private TextView item_name, item_description, item_reward;
    private Button item_btn;
    private ImageView item_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        Bundle bundle = getIntent().getExtras();
        int item_id = bundle.getInt("item_id", -1);

        if (item_id == -1) {
            Toast.makeText(ItemActivity.this, "Item not found", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ItemActivity.this, RewardsActivity.class);
            startActivity(intent);
        }

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
                item_reward.setText("Choosing this item, will remove " + item.getReward() + " fidelity points from your account.");

                String url = "https://becomeacookmaster.live/assets/images/shop-items/" + item.getImage();

                Picasso.get().load(url).into(item_image);

                item_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "Item reserved", Toast.LENGTH_SHORT).show();
                       // CHECK ITEM STOCK AGAIN
                        // REMOVE POINTS
                        // SEND EMAIL SAYING YOU CAN COME FETCH UR ITEM.
                        //UPDATE ITEM STOCK
                    }
                });
            }

            public void onError(String message) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getItem(int item_id, ItemCallback item_reserved) {

        RequestQueue rq = Volley.newRequestQueue(ItemActivity.this);

        String url = "https://api.becomeacookmaster.live:9000/shopitem/"+ Integer.toString(item_id);

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
                            Toast.makeText(ItemActivity.this,"ERROR1: %s".format(e.toString()) , Toast.LENGTH_SHORT).show();
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