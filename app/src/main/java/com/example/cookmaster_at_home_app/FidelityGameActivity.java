package com.example.cookmaster_at_home_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import at.favre.lib.crypto.bcrypt.BCrypt;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class FidelityGameActivity extends AppCompatActivity {

    private KonfettiView konfettiView;
    private EditText answerFG;
    private Button submitFG, settingsButton;
    private LinearLayout layout;
    private int tryCount = 0;
    private final int FIDELITY_POINTS_EARNED = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fidelity_game);

        int user_id = getIntent().getIntExtra("user_id", -1);
        int subscriptionId = getIntent().getIntExtra("subscription_id", -1);
        int fidelityPoints = getIntent().getIntExtra("fidelitypoints", 0);
        boolean auto_reconnect = getIntent().getBooleanExtra("auto_reconnect", false);

        if (fidelityPoints == 0 || user_id == -1 || subscriptionId == -1) {
            Toast.makeText(FidelityGameActivity.this, "Error, please log in again", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(FidelityGameActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        settingsButton = findViewById(R.id.settings_button);
        answerFG = findViewById(R.id.fidelity_game_answer);
        submitFG = findViewById(R.id.fidelity_game_button);
        konfettiView = findViewById(R.id.konfettiView);
        LinearLayout layout = findViewById(R.id.fidelity_game_layout);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextPage = new Intent(FidelityGameActivity.this, AccountActivity.class);
                nextPage.putExtra("user_id", user_id);
                nextPage.putExtra("subscription_id", subscriptionId);
                nextPage.putExtra("auto_reconnect", auto_reconnect);
                startActivity(nextPage);
            }
        });


        submitFG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String answer = answerFG.getText().toString();

                if (answer.equals("Apple - Royal Gala")) {

                    try {
                        SharedPreferences sharedPreferences = getSharedPreferences("fidelity-game", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                        String dateSimple = formatter.format(date);
                        editor.putString(Integer.toString(user_id), dateSimple);
                        editor.apply();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }


                    if (tryCount == 0){
                        updateFidelityPoints(user_id, FIDELITY_POINTS_EARNED * 2 + fidelityPoints);
                    } else  {
                        updateFidelityPoints(user_id, FIDELITY_POINTS_EARNED + fidelityPoints);
                    }

                    EmitterConfig emitterConfig = new Emitter(5L, TimeUnit.SECONDS).perSecond(50);
                    Party party = new PartyFactory(emitterConfig)
                            .angle(270)
                            .spread(90)
                            .setSpeedBetween(1f, 5f)
                            .timeToLive(2000L)
                            .position(0.0, 0.0, 1.0, 0.0)
                            .build();
                    konfettiView.start(party);
                    Toast.makeText(FidelityGameActivity.this, "You won !", Toast.LENGTH_SHORT).show();
                    answerFG.setText("");

                    submitFG.setClickable(false);
                    submitFG.setVisibility(View.GONE);

                    Button back = new Button(FidelityGameActivity.this);
                    back.setText("Back");
                    back.setBackgroundColor(getColor(R.color.brand_primary_color));
                    back.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(FidelityGameActivity.this, FidelityOverviewActivity.class);
                            intent.putExtra("user_id", user_id);
                            intent.putExtra("subscription_id", subscriptionId);
                            intent.putExtra("auto_reconnect", auto_reconnect);
                            startActivity(intent);
                        }
                    });
                    back.setVisibility(View.VISIBLE);
                    back.setClickable(true);
                    layout.addView(back);

                } else {
                    if (tryCount++ == 1){
                        Intent intent = new Intent(FidelityGameActivity.this, FidelityOverviewActivity.class);
                        startActivity(intent);
                    }
                    answerFG.setText("");
                    Toast.makeText(FidelityGameActivity.this, "Wrong answer, you have one more try!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void updateFidelityPoints(int user_id, int i) {
        Map<String, Integer> params = new HashMap<String, Integer>();
        //ADD PARAMS HERE
        params.put("fidelitypoints", i);

        RequestQueue rq = Volley.newRequestQueue(FidelityGameActivity.this);

        String url = "https://api.becomeacookmaster.live:9000/client/" + Integer.toString(user_id);

        JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.PATCH ,url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //nothing to do
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(FidelityGameActivity.this, "ERROR 3: %s".format(error.toString()), Toast.LENGTH_SHORT).show();
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
}