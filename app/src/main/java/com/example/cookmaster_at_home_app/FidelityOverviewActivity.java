package com.example.cookmaster_at_home_app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class FidelityOverviewActivity extends AppCompatActivity {

    private Button btn_rewards;
    private TextView txt_fidelity_points;
    private TextView needed_points;
    private ImageButton about_fidelity;
    private ImageView stage50,stage110,stage180;
    private KonfettiView konfettiView = null;
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    Button btn_secret, settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fidelity_overview);

        int user_id = getIntent().getIntExtra("user_id", -1);
        int subscriptionId = getIntent().getIntExtra("subscription_id", -1);
        boolean auto_reconnect = getIntent().getBooleanExtra("auto_reconnect", false);

        nfcAdapter = NfcAdapter.getDefaultAdapter(FidelityOverviewActivity.this);

        //DEBUGGING: CHECK IF NFC IS AVAILABLE
//        if(nfcAdapter != null && nfcAdapter.isEnabled()){
//            Toast.makeText(this, "NFC available!", Toast.LENGTH_LONG).show();
//        }
        //Create a PendingIntent object so the Android system can
        //populate it with the details of the tag when it is scanned.
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);

        btn_rewards = findViewById(R.id.stages_btn);
        txt_fidelity_points = findViewById(R.id.user_fidelity_points);
        needed_points = findViewById(R.id.needed_points);
        about_fidelity = findViewById(R.id.about_fidelity_btn);
        settingsButton = findViewById(R.id.settings_button);
        btn_secret = findViewById(R.id.secret_game_btn);
        btn_secret.setVisibility(View.GONE);
        btn_secret.setClickable(false);

        konfettiView = findViewById(R.id.konfettiView);

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

        about_fidelity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(FidelityOverviewActivity.this)
                        .setTitle(getResources().getString(R.string.popUpTitle))
                        .setMessage(getResources().getString(R.string.popUpMessage))
                        .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cookmastershop), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                                String url = "https://becomeacookmaster.live";
                                browserIntent.setData(Uri.parse(url));
                                startActivity(browserIntent);
                            }
                        })
                        .show();

            }
        });



        settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextPage = new Intent(FidelityOverviewActivity.this, AccountActivity.class);
                nextPage.putExtra("user_id", user_id);
                nextPage.putExtra("subscription_id", subscriptionId);
                nextPage.putExtra("auto_reconnect", auto_reconnect);
                startActivity(nextPage);
            }
        });

        getFidelityPoints(user_id , new FidelityCallback() {
            @Override
            public void onSuccess(int id, String firstname,String lastname, String email, int fidelity_points) {

                Client client = new Client(id, email, firstname, lastname, fidelity_points);

                matrix.setSaturation(1);
                ColorMatrixColorFilter filter_color = new ColorMatrixColorFilter(matrix);

                btn_secret.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (hasAlreadyPlayed(user_id) == false) {
                            Intent intent = new Intent(FidelityOverviewActivity.this, FidelityGameActivity.class);
                            intent.putExtra("user_id", user_id);
                            intent.putExtra("fidelitypoints", client.getFidelity_points());
                            intent.putExtra("subscription_id", subscriptionId);
                            intent.putExtra("auto_reconnect", auto_reconnect);
                            startActivity(intent);
                        } else {
                            Toast.makeText(FidelityOverviewActivity.this, "You have already played today!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                btn_rewards.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FidelityOverviewActivity.this, RewardsActivity.class);
                        intent.putExtra("user_id", user_id);
                        intent.putExtra("subscription_id", subscriptionId);
                        intent.putExtra("auto_reconnect", auto_reconnect);
                        intent.putExtra("fidelitypoints", client.getFidelity_points());
                        startActivity(intent);
                    }
                });

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

    private boolean hasAlreadyPlayed(int user_id) {
        SharedPreferences sharedPreferences = getSharedPreferences("fidelity-game", MODE_PRIVATE);
        if (sharedPreferences.contains(Integer.toString(user_id))){
            String lastTime = sharedPreferences.getString(Integer.toString(user_id), "undefined");
            if (lastTime.equals("undefined")) {
                return false;
            } else {
                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                String today = formatter.format(date);
                System.out.println(lastTime + " " + today);
                if (lastTime.equals(today)) {
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

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

        EmitterConfig emitterConfig = new Emitter(5L, TimeUnit.SECONDS).perSecond(50);
        Party party = new PartyFactory(emitterConfig)
                .angle(270)
                .spread(90)
                .setSpeedBetween(1f, 5f)
                .timeToLive(2000L)
                .position(0.0, 0.0, 1.0, 0.0)
                .build();
        konfettiView.start(party);

        btn_secret.setVisibility(View.VISIBLE);
        btn_secret.setClickable(true);

//        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
//            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
//            NdefMessage[] msgs;
//            String debug = "";
//            if (rawMsgs != null) {
//                msgs = new NdefMessage[rawMsgs.length];
//                for (int i = 0; i < rawMsgs.length; i++) {
//                    msgs[i] = (NdefMessage) rawMsgs[i];
//                    debug += msgs[i].toString();
//                }
//                Toast.makeText(this, debug, Toast.LENGTH_LONG).show();
//            }
//        } else {
//            Toast.makeText(this, "NFC intent received but not tag discovered!", Toast.LENGTH_LONG).show();
//        }
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