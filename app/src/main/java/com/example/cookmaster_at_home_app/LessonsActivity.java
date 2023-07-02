package com.example.cookmaster_at_home_app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

public class LessonsActivity extends AppCompatActivity {

    private List<Lesson> lessons;
    private ListView listLessons;
    private TextView debug;

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

            listLessons = findViewById(R.id.listLessons);
            debug = findViewById(R.id.title);

             this.lessons =  getLessons();
             //or get it from the shared preferences if it's already there
             // this.lessons =  getLessonsFromClient();

            // make the code pause a bit cuz the request is async if need be
            //replace this with a callback ?
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
//                    Toast.makeText(LessonsActivity.this, Integer.toString(counter), Toast.LENGTH_SHORT).show();
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
                                    if (alreadyWatched || clientSubscriptionName.equals("Master"))
                                    {
                                        Toast.makeText(LessonsActivity.this, "You have unlimited access to this lesson today.", Toast.LENGTH_LONG).show();
                                        Intent nextPage = new Intent(LessonsActivity.this, LessonActivity.class);
                                        nextPage.putExtra("name",lesson.getName());
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
                                        //group/image/ytb to add?
                                        startActivity(nextPage);
                                    }
                                    else
                                    {
                                        if (clientSubscriptionMaxLessons > counter) {
                                            //USER CAN WATCH LESSON
                                            displayPopUp(clientId, clientFullname, clientEmail, clientSubscriptionName, clientSubscriptionMaxLessons, counter ,lesson);
                                        } else {
                                            //USER CANNOT WATCH LESSON
                                            Toast.makeText(LessonsActivity.this, "You have reached your daily limit of lessons ("+clientSubscriptionMaxLessons+"). Update your subscription or wait a bit", Toast.LENGTH_LONG).show();
                                            //TODO : REDIRECT TO LESSONS HE CAN WATCH TODAY ?
                                        }
                                    }
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    System.out.println("dommage");
                                }
                            });
                        }
                    });
                }
                @Override
                public void onError(String error) {
                    System.out.println("dommage");;
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
    public void displayPopUp(int clientId, String clientFullname, String clientEmail, String clientSubscriptionName, int clientSubscriptionMaxLessons, int clientWatchedLessons, Lesson lesson){
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
                        nextPage.putExtra("subscription_maxlessonaccess", clientSubscriptionMaxLessons);
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
                            Toast.makeText(LessonsActivity.this, message, Toast.LENGTH_SHORT).show();
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

                           //Toast.makeText(LessonsActivity.this, response, Toast.LENGTH_SHORT).show();
                            JSONArray json = new JSONArray(response);
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