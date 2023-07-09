package com.example.cookmaster_at_home_app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LessonsActivity extends AppCompatActivity {

    private List<Lesson> lessons;
    private ListView listLessons;
    private TextView debug;

    private Button settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons);

        Bundle extras = getIntent().getExtras();
        if (extras == null)
        {
            //redirect to login page
            Intent nextPage = new Intent(LessonsActivity.this, LoginActivity.class);
            startActivity(nextPage);
        }
            else
        {
            int clientId = extras.getInt("user_id");
            String clientFullname = extras.getString("fullname");
            String clientEmail = extras.getString("email");
            String clientSubscriptionName = extras.getString("subscription_name");
            int clientSubscriptionMaxLessons = extras.getInt("subscription_maxlessonaccess");
            int clientSubscriptionId = extras.getInt("subscription_id");
            boolean auto_reconnect = extras.getBoolean("auto_reconnect");

            listLessons = findViewById(R.id.listLessons);
            debug = findViewById(R.id.title);
            settingsButton = findViewById(R.id.settings_button);
            //if we have internet connection we fetch the lessons from the server else from shared prefrences

            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent nextPage = new Intent(LessonsActivity.this, AccountActivity.class);
                    nextPage.putExtra("user_id", clientId);
                    nextPage.putExtra("subscription_id", clientSubscriptionId);
                    nextPage.putExtra("auto_reconnect", auto_reconnect);
                    startActivity(nextPage);
                }
            });

            if (NetworkHelper.isNetworkAvailable(this)) {
                this.lessons = getLessons();
            } else {
                this.lessons = getLessonsFromSharedPrefrences();
                LessonAdapter lesson_adapter = new LessonAdapter(lessons,LessonsActivity.this);
                listLessons.setAdapter(lesson_adapter);
                listLessons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(LessonsActivity.this, "No internet connection detected. This lesson will be available when you have internet.", Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }

            // make the code pause a bit cuz the request is async if need be
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            RequestQueue rq = Volley.newRequestQueue(LessonsActivity.this);

            //UPDATE CLIENT WATCHED LESSONS  (remove entries older than a day)
            updateClientWatchedLessons(rq, clientId);

            LessonClientCallback callback = new LessonClientCallback(){
                @Override
                public void onSuccess(int counter, List<Lesson> lessons) {
                    LessonAdapter lesson_adapter = new LessonAdapter(lessons,LessonsActivity.this);
                    listLessons.setAdapter(lesson_adapter);


                    //MAKE LESSONS CLICKABLE
                    listLessons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            Lesson lesson = (Lesson) parent.getItemAtPosition(position) ;
                            //CHECK IF USER ALREADY WATCHED HIS THING.

                                hasAlreadyWatchLesson(clientId, lesson, new AlreadyWatchedCallback() {
                                    @Override
                                    public void onSuccess(boolean alreadyWatched, Lesson lesson) {
                                        if (alreadyWatched || clientSubscriptionName.equals("Master")) {
                                            Toast.makeText(LessonsActivity.this, "You have unlimited access to this lesson today.", Toast.LENGTH_LONG).show();
                                            Intent nextPage = new Intent(LessonsActivity.this, LessonActivity.class);
                                            nextPage.putExtra("name", lesson.getName());
                                            nextPage.putExtra("description", lesson.getDescription());
                                            nextPage.putExtra("content", lesson.getContent());
                                            nextPage.putExtra("author", lesson.getAuthor());
                                            nextPage.putExtra("difficulty", lesson.getDifficulty());
                                            nextPage.putExtra("picture", lesson.getImage());
                                            nextPage.putExtra("id_group", lesson.getGroup());
                                            //client extras might not need this:
                                            nextPage.putExtra("fullname", clientFullname);
                                            nextPage.putExtra("user_id", clientId);
                                            nextPage.putExtra("email", clientEmail);
                                            nextPage.putExtra("subscription_name", clientSubscriptionName);
                                            nextPage.putExtra("subscription_id", clientSubscriptionId);
                                            nextPage.putExtra("subscription_maxlessonaccess", clientSubscriptionMaxLessons);

                                            startActivity(nextPage);
                                        } else {
                                            if (clientSubscriptionMaxLessons > counter) {
                                                //USER CAN WATCH LESSON
                                                displayPopUp(clientId, clientSubscriptionId, clientFullname, clientEmail, clientSubscriptionName, clientSubscriptionMaxLessons, counter, lesson);
                                            } else {
                                                //USER CANNOT WATCH LESSON
                                                Toast.makeText(LessonsActivity.this, "You have reached your daily limit of lessons (" + clientSubscriptionMaxLessons + "). Update your subscription or wait a bit", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onError(String errorMessage) {

                                    }
                                });
                            }
                    });
                }
                @Override
                public void onError(String error) {

                }
            };
            //GET CLIENT WATCHED LESSONS
            countClientWatchedLessons(rq, clientId, callback);

        }
    }
    private void hasAlreadyWatchLesson(int clientId, Lesson lesson, AlreadyWatchedCallback callback) {
        RequestQueue rq = Volley.newRequestQueue(LessonsActivity.this);

        String url = "https://api.becomeacookmaster.live:9000/lesson/watch/" + Integer.toString(clientId) + "/" + Integer.toString(lesson.getId());

        StringRequest query = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean alreadyWatched = jsonResponse.getBoolean("iswatched");
                            callback.onSuccess(alreadyWatched, lesson);
                        } catch (Exception e) {
                            Toast.makeText(LessonsActivity.this, "ERROR 1: %s".format(e.toString()), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null){
                            String errorMessage = new String(error.networkResponse.data);
                            Toast.makeText(LessonsActivity.this, url +"NOT ok" + errorMessage, Toast.LENGTH_SHORT).show();
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

    private void countClientWatchedLessons(RequestQueue rq, int clientId, LessonClientCallback callback) {

        String url = "https://api.becomeacookmaster.live:9000/lesson/views/" + Integer.toString(clientId);

        StringRequest query = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            int lessonWatched = jsonResponse.getInt("count");
                            callback.onSuccess(lessonWatched, lessons);
                        } catch (Exception e) {
                            Toast.makeText(LessonsActivity.this, "ERROR 1: %s".format(e.toString()), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null){
                            String errorMessage = new String(error.networkResponse.data);
                            Toast.makeText(LessonsActivity.this, url +"NOT ok" + errorMessage, Toast.LENGTH_SHORT).show();
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

    private void updateClientWatchedLessons(RequestQueue rq, int clientId){

        String url = "https://api.becomeacookmaster.live:9000/lesson/views/" + Integer.toString(clientId);

        StringRequest query = new StringRequest(Request.Method.DELETE,
                url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null){
                            String errorMessage = new String(error.networkResponse.data);
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
    public void displayPopUp(int clientId,int clientSubscriptionId, String clientFullname, String clientEmail, String clientSubscriptionName, int clientSubscriptionMaxLessons, int clientWatchedLessons, Lesson lesson){
        new AlertDialog.Builder(LessonsActivity.this)
                .setTitle(getResources().getString(R.string.confirm_show_lesson))
                .setMessage("With your " + clientSubscriptionName +" subscription, you have " + clientSubscriptionMaxLessons + " lessons access.\nYou have watched " + clientWatchedLessons + " lessons today. Do you want to watch " + lesson.getName() + " ?")
                .setPositiveButton(getResources().getString(R.string.popUpYes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //update client watched lessons
                        increaseClientWatchedLessons(clientId, lesson.getId());
                        //go to the lesson page
                        Intent nextPage = new Intent(LessonsActivity.this, LessonActivity.class);
                        nextPage.putExtra("name",lesson.getName());
                        nextPage.putExtra("idlesson",lesson.getId());
                        nextPage.putExtra("description", lesson.getDescription());
                        nextPage.putExtra("content", lesson.getContent());
                        nextPage.putExtra("author", lesson.getAuthor());
                        nextPage.putExtra("difficulty", lesson.getDifficulty());
                        nextPage.putExtra("picture", lesson.getImage());
                        nextPage.putExtra("id_group", lesson.getGroup());
                        //client extras might not need this:
                        nextPage.putExtra("fullname", clientFullname);
                        nextPage.putExtra("user_id", clientId);
                        nextPage.putExtra("email", clientEmail);
                        nextPage.putExtra("subscription_name", clientSubscriptionName);
                        nextPage.putExtra("subscription_id", clientSubscriptionId);
                        nextPage.putExtra("subscription_maxlessonaccess", clientSubscriptionMaxLessons);
                        nextPage.putExtra("auto_reconnect", getIntent().getBooleanExtra("auto_reconnect", false));
                        //group/image/ytb to add?
                        startActivity(nextPage);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.popUpNo), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel the dialog
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void increaseClientWatchedLessons(int clientId, int lessonId) {

        RequestQueue rq = Volley.newRequestQueue(LessonsActivity.this);

        String url = "https://api.becomeacookmaster.live:9000/client/watch/" + Integer.toString(clientId) + "/" + Integer.toString(lessonId);
        StringRequest query = new StringRequest(Request.Method.PATCH,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String message = jsonResponse.getString("message");
                        } catch (Exception e) {
                            Toast.makeText(LessonsActivity.this, "ERROR 1: %s".format(e.toString()), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null){
                            String errorMessage = new String(error.networkResponse.data);
                            Toast.makeText(LessonsActivity.this, url +"NOT ok" + errorMessage, Toast.LENGTH_SHORT).show();
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

    private List<Lesson> getLessonsFromSharedPrefrences() {
        SharedPreferences sharedPreferences = getSharedPreferences("lessons", Context.MODE_PRIVATE);
        List<Lesson> list = new ArrayList<>();

        try {
            JSONArray json = new JSONArray(sharedPreferences.getString("lessons", null));

            for (int i = 0; i < json.length(); i++) {
                JSONObject obj = json.getJSONObject(i);
                int id = obj.getInt("idlesson");
                String name = obj.getString("name");
                String description = obj.getString("description");
                int difficulty = obj.getInt("difficulty");
                String content = obj.getString("content");
                String author_firstname = obj.getString("firstname");
                String author_lastname = obj.getString("lastname");
                int group = obj.getInt("idlessongroup");
                String image = obj.getString("picture");
                list.add(new Lesson(name, id, description, image, difficulty, content, author_firstname + " " + author_lastname, group));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
    public List<Lesson> getLessons(){
        List<Lesson> list = new ArrayList<>();
        RequestQueue rq = Volley.newRequestQueue(LessonsActivity.this);


        String url = "https://api.becomeacookmaster.live:9000/lesson/all";

        StringRequest query = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {

                            JSONArray json = new JSONArray(response);

                            //SAVE DATA SO WE CAN STILL SEE IT OFFLINE if it is not the same string.
                            SharedPreferences sharedPreferences = getSharedPreferences("lessons", Context.MODE_PRIVATE);

                            if (!sharedPreferences.getString("lessons", "null").equals(response)){
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("lessons", json.toString());
                                editor.apply();
                            }

                            for (int i = 0; i < json.length(); i++) {
                                JSONObject obj = json.getJSONObject(i);
                                int id = obj.getInt("idlesson");
                                String name = obj.getString("name");
                                String description = obj.getString("description");
                                int difficulty = obj.getInt("difficulty");
                                String content = obj.getString("content");
                                String author_firstname = obj.getString("firstname");
                                String author_lastname = obj.getString("lastname");
                                int group = obj.getInt("idlessongroup");
                                String image = obj.getString("picture");
                                list.add(new Lesson(name, id, description, image, difficulty, content, author_firstname + " " + author_lastname, group));
                            }

                        }catch (Exception e){
                            Toast.makeText(LessonsActivity.this,"ERROR1: %s".format(e.toString()) , Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null){
                            String errorMessage = new String(error.networkResponse.data);
                            Toast.makeText(LessonsActivity.this, "ok"+errorMessage, Toast.LENGTH_SHORT).show();
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
        return list;
    }
}