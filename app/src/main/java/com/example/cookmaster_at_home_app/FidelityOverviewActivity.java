package com.example.cookmaster_at_home_app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FidelityOverviewActivity extends AppCompatActivity {

    private Button btn_rewards;
    private TextView txt_fidelity_points;
    private TextView needed_points;
    private ImageButton about_fidelity;
    private ImageView stage50,stage110,stage180;

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;

    final static String TAG = "nfc_test"; //debug
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fidelity_overview);

        int user_id = getIntent().getIntExtra("user_id", -1);
        int subscriptionId = getIntent().getIntExtra("subscription_id", -1);

        nfcAdapter = NfcAdapter.getDefaultAdapter(FidelityOverviewActivity.this);

        //DEBUGGING: CHECK IF NFC IS AVAILABLE
        if(nfcAdapter != null && nfcAdapter.isEnabled()){
            Toast.makeText(this, "NFC available!", Toast.LENGTH_LONG).show();
        }
        //Create a PendingIntent object so the Android system can
        //populate it with the details of the tag when it is scanned.

        pendingIntent = PendingIntent.getActivity(FidelityOverviewActivity.this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE);

        btn_rewards = findViewById(R.id.stages_btn);
        txt_fidelity_points = findViewById(R.id.user_fidelity_points);
        needed_points = findViewById(R.id.needed_points);
        about_fidelity = findViewById(R.id.about_fidelity_btn);

        stage50 = findViewById(R.id.stage_50);
        stage110 = findViewById(R.id.stage_110);
        stage180 = findViewById(R.id.stage_180);

        //BY DEFAULT ALL IMAGES ARE GRAYSCALE TO SHOW THE USER THAT HE HAS NOT REACHED THE STAGE YET
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        stage50.setColorFilter(filter);
        stage110.setColorFilter(filter);
        stage180.setColorFilter(filter);

        btn_rewards.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               Intent intent = new Intent(FidelityOverviewActivity.this, RewardsActivity.class);
                                               intent.putExtra("user_id", user_id);
                                               startActivity(intent);
                                           }
                                       });

        getFidelityPoints(user_id , new FidelityCallback() {
            @Override
            public void onSuccess(int id, String firstname,String lastname, String email, int fidelity_points) {

                Client client = new Client(id, email, firstname, lastname, fidelity_points);

                matrix.setSaturation(1);
                ColorMatrixColorFilter filter_color = new ColorMatrixColorFilter(matrix);

            txt_fidelity_points.setText("Hello " + client.getLastname() + ", you have " + Integer.toString(client.getFidelity_points()) + " fidelity points");
                if (client.getFidelity_points() < 50) {
                    needed_points.setText("You need " + Integer.toString(50 - client.getFidelity_points()) + " points to unlock the next stage");

                } else if (client.getFidelity_points() < 110) {
                    stage50.setColorFilter(filter_color);
                    needed_points.setText("You need " + Integer.toString(110 - client.getFidelity_points()) + " points to unlock the next stage");

                } else if (client.getFidelity_points() < 180) {
                    stage50.setColorFilter(filter_color);
                    stage110.setColorFilter(filter_color);
                    needed_points.setText("You need " + Integer.toString(180 - client.getFidelity_points()) + " points to unlock the next stage");
                } else {
                    stage50.setColorFilter(filter_color);
                    stage110.setColorFilter(filter_color);
                    stage180.setColorFilter(filter_color);
                    needed_points.setText("You have unlocked every stages !");
                }
            }
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(FidelityOverviewActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        assert nfcAdapter != null;
        nfcAdapter.enableForegroundDispatch(FidelityOverviewActivity.this,pendingIntent,null,null);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(FidelityOverviewActivity.this);
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Toast.makeText(this, "NFC intent received!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "NFC intent received but not tag discovered!", Toast.LENGTH_LONG).show();
        }
    }

    private void getFidelityPoints(int user_id, FidelityCallback callback) {

            RequestQueue rq = Volley.newRequestQueue(FidelityOverviewActivity.this);
            String url = "https://api.becomeacookmaster.live:9000/client/" + Integer.toString(user_id);

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
                                    int fidelitypoints = json_response.getInt("fidelitypoints");
                                    int id = json_response.getInt("idusers");
                                    callback.onSuccess(id, lastname, firstname, email, fidelitypoints);
                                }
                            }catch (Exception e){
                                Toast.makeText(FidelityOverviewActivity.this,"ERROR 2: %s".format(e.toString()) , Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(FidelityOverviewActivity.this,"ERROR: %s".format(error.toString()) , Toast.LENGTH_SHORT).show();
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