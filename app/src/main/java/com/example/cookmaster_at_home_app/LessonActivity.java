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
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LessonActivity extends AppCompatActivity {

    private TextView lesson_name;
    private TextView lesson_description;
    private TextView lesson_author;
    private TextView lesson_content;
    private Button back_button;
    private Button settingsButton;

    private ListView lessonGroupListView;
    private List<Lesson> GroupLessons;

    private ImageView lesson_image;
    private boolean auto_reconnect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        Bundle extras = getIntent().getExtras();
        String title = extras.getString("name");
        int idlesson = extras.getInt("idlesson");
        String description = extras.getString("description");
        String content = extras.getString("content");
        String author = extras.getString("author");
        String image = extras.getString("picture");
        int difficulty = extras.getInt("difficulty");
        String clientFullname = extras.getString("fullname");
        int groupId = extras.getInt("id_group");
        int clientId = extras.getInt("user_id");
        String clientEmail = extras.getString("email");
        int clientSubscriptionId = extras.getInt("subscription_id");
        String clientSubscriptionName = extras.getString("subscription_name");
        int clientSubscriptionMaxLessons = extras.getInt("subscription_maxlessonaccess");
        auto_reconnect = extras.getBoolean("auto_reconnect");

        settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextPage = new Intent(LessonActivity.this, AccountActivity.class);
                nextPage.putExtra("user_id", clientId);
                nextPage.putExtra("subscription_id", clientSubscriptionId);
                nextPage.putExtra("auto_reconnect", auto_reconnect);
                startActivity(nextPage);
            }
        });

        Lesson lesson = new Lesson(title, idlesson, description, image, difficulty, content, author, groupId);

        lesson_name = findViewById(R.id.lesson_title);
        lesson_description = findViewById(R.id.lesson_description);
        lesson_author = findViewById(R.id.lesson_author);
        lesson_content = findViewById(R.id.lesson_content);

        LinearLayout starsLayout = findViewById(R.id.difficulty_stars);

        int starSizeInPixels = getResources().getDimensionPixelSize(R.dimen.star_size);

        for (int i = 0; i < difficulty; i++) {
            ImageView starImageView = new ImageView(this);
            starImageView.setImageResource(R.drawable.star_icon_blue);
            starImageView.setLayoutParams(new LinearLayout.LayoutParams(starSizeInPixels, starSizeInPixels));
            starsLayout.addView(starImageView);
        }

        lesson_name.setText(title);
        lesson_description.setText("Description: "+ description);
        lesson_author.setText("by " + author);
        lesson_content.setText(content);

        //DISPLAY ALL LESSONS FROM THE GROUP
        lessonGroupListView = findViewById(R.id.listGroupLessons);
        RequestQueue rq = Volley.newRequestQueue(LessonActivity.this);
        //UPDATE CLIENT WATCHED LESSONS  (remove entries older than a day)
        updateClientWatchedLessons(rq, clientId);

        LessonClientCallback callback = new LessonClientCallback() {
            @Override
            public void onSuccess(int counter, List<Lesson> lessons) {
                getLessonsByGroup(groupId, new LessonGroupCallback() {
                    @Override
                    public void onSuccess(List<Lesson> result) {
                        GroupLessons = result;
                        LessonAdapter lesson_adapter = new LessonAdapter(GroupLessons, LessonActivity.this);
                        lessonGroupListView.setAdapter(lesson_adapter);
                        lessonGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                Lesson lesson = (Lesson) parent.getItemAtPosition(position);
                                //CHECK IF USER ALREADY WATCHED HIS THING.
                                hasAlreadyWatchLesson(clientId, lesson, new AlreadyWatchedCallback() {
                                    @Override
                                    public void onSuccess(boolean alreadyWatched, Lesson lesson) {
                                        if (alreadyWatched || clientSubscriptionName.equals("Master")) {
                                            if (alreadyWatched) {
                                                Toast.makeText(LessonActivity.this, "You have unlimited access to this lesson today.", Toast.LENGTH_LONG).show();
                                            }
                                            Intent nextPage = new Intent(LessonActivity.this, LessonActivity.class);
                                            nextPage.putExtra("name", lesson.getName());
                                            nextPage.putExtra("description", lesson.getDescription());
                                            nextPage.putExtra("content", lesson.getContent());
                                            nextPage.putExtra("author", lesson.getAuthor());
                                            nextPage.putExtra("difficulty", lesson.getDifficulty());
                                            nextPage.putExtra("picture", lesson.getImage());
                                            nextPage.putExtra("id_group", lesson.getGroup());

                                            nextPage.putExtra("fullname", clientFullname);
                                            nextPage.putExtra("user_id", clientId);
                                            nextPage.putExtra("subscription_id", clientSubscriptionId);
                                            nextPage.putExtra("email", clientEmail);
                                            nextPage.putExtra("subscription_name", clientSubscriptionName);
                                            nextPage.putExtra("subscription_maxlessonaccess", clientSubscriptionMaxLessons);
                                            nextPage.putExtra("auto_reconnect", auto_reconnect);

                                            startActivity(nextPage);
                                        } else {
                                            if (clientSubscriptionMaxLessons > counter) {
                                                //USER CAN WATCH LESSON
                                                displayPopUp(clientId, clientFullname, clientEmail, clientSubscriptionName, clientSubscriptionMaxLessons, counter, lesson);
                                            } else {
                                                //USER CANNOT WATCH LESSON
                                                Toast.makeText(LessonActivity.this, "You have reached your daily limit of lessons (" + clientSubscriptionMaxLessons + "). Update your subscription or wait a bit", Toast.LENGTH_LONG).show();
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
                    public void onError(String message) {
                        Toast.makeText(LessonActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {

            }
        };
        //GET CLIENT WATCHED LESSONS
        countClientWatchedLessons(rq, clientId, callback);


        back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextPage = new Intent(LessonActivity.this, LessonsActivity.class);
                nextPage.putExtra("fullname", clientFullname);
                nextPage.putExtra("user_id", clientId);
                nextPage.putExtra("email", clientEmail);
                nextPage.putExtra("subscription_name", clientSubscriptionName);
                nextPage.putExtra("subscription_maxlessonaccess", clientSubscriptionMaxLessons);
                nextPage.putExtra("auto_reconnect", auto_reconnect);
                startActivity(nextPage);
            }
        });
    }
    private void hasAlreadyWatchLesson(int clientId, Lesson lesson, AlreadyWatchedCallback callback) {
        RequestQueue rq = Volley.newRequestQueue(LessonActivity.this);

        String url = "https://api.becomeacookmaster.live:9000/lesson/watch/" + clientId + "/" + lesson.getId();

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
                            Toast.makeText(LessonActivity.this, String.format(e.toString()), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null){
                            String errorMessage = new String(error.networkResponse.data);
                            Toast.makeText(LessonActivity.this, url +"NOT ok" + errorMessage, Toast.LENGTH_SHORT).show();
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

        String url = "https://api.becomeacookmaster.live:9000/lesson/views/" + clientId;

        StringRequest query = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            int lessonWatched = jsonResponse.getInt("count");
                            callback.onSuccess(lessonWatched, GroupLessons);
                        } catch (Exception e) {
                            Toast.makeText(LessonActivity.this, String.format(e.toString()), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null){
                            String errorMessage = new String(error.networkResponse.data);
                            Toast.makeText(LessonActivity.this, url +"NOT ok" + errorMessage, Toast.LENGTH_SHORT).show();
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

        String url = "https://api.becomeacookmaster.live:9000/lesson/views/" + clientId;

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
                            Toast.makeText(LessonActivity.this, url +"NOT ok" + errorMessage, Toast.LENGTH_SHORT).show();
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
    public void displayPopUp(int clientId, String clientFullname, String clientEmail, String clientSubscriptionName, int clientSubscriptionMaxLessons, int clientWatchedLessons, Lesson lesson){
        new AlertDialog.Builder(LessonActivity.this)
                .setTitle(getResources().getString(R.string.confirm_show_lesson))
                .setMessage("With your " + clientSubscriptionName +" subscription, you have " + clientSubscriptionMaxLessons + " lessons access.\nYou have watched " + clientWatchedLessons + " lessons today. Do you want to watch " + lesson.getName() + " ?")
                .setPositiveButton(getResources().getString(R.string.popUpYes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //update client watched lessons
                        increaseClientWatchedLessons(clientId, lesson.getId());
                        //go to the lesson page
                        Intent nextPage = new Intent(LessonActivity.this, LessonActivity.class);
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
                        nextPage.putExtra("subscription_maxlessonaccess", clientSubscriptionMaxLessons);
                        nextPage.putExtra("auto_reconnect", auto_reconnect);
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

        RequestQueue rq = Volley.newRequestQueue(LessonActivity.this);

        String url = "https://api.becomeacookmaster.live:9000/client/watch/" + clientId + "/" + lessonId;
        StringRequest query = new StringRequest(Request.Method.PATCH,
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
                            Toast.makeText(LessonActivity.this, url +"NOT ok" + errorMessage, Toast.LENGTH_SHORT).show();
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

    private void getLessonsByGroup(int groupId, LessonGroupCallback callback) {
        RequestQueue rq = Volley.newRequestQueue(LessonActivity.this);
        List<Lesson> list = new ArrayList<>();

        String url = "https://api.becomeacookmaster.live:9000/lesson/group/" + groupId;

        StringRequest query = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {

                            JSONArray json = new JSONArray(response);

                            for (int i = 0; i < json.length(); i++) {
                                JSONObject obj = json.getJSONObject(i);
                                int id = obj.getInt("idlesson");
                                String name = obj.getString("name");
                                String description = obj.getString("description");
                                int difficulty = obj.getInt("difficulty");
                                String content = obj.getString("content");
                                String firstname = obj.getString("firstname");
                                String lastname = obj.getString("lastname");
                                int group = obj.getInt("idlessongroup");
                                String image = obj.getString("picture");
                                list.add(new Lesson(name, id, description, image, difficulty, content, firstname+" " +lastname, group));
                                callback.onSuccess(list);
                            }
                        }catch (Exception e){
                            Toast.makeText(LessonActivity.this, String.format(e.toString()) , Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null){
                            String errorMessage = new String(error.networkResponse.data);
                            Toast.makeText(LessonActivity.this, "ok"+errorMessage, Toast.LENGTH_SHORT).show();
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