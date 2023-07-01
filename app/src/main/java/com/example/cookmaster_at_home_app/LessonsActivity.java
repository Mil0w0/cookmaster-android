package com.example.cookmaster_at_home_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
        Bundle extras = getIntent().getExtras();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons);
        listLessons = findViewById(R.id.listLessons);
        debug = findViewById(R.id.title);

         this.lessons =  getLessons();
         //or get it from the shared preferences if it's already there
         // this.lessons =  getLessonsFromClient();

//        List<Lesson> list =new ArrayList<>();
//        list.add(new Lesson("Lesson name", 1, "Lesson description", "Lesson image", 2, "Lesson content", "Lesson author", "Lesson group"));
//        list.add(new Lesson("Lesson name 2", 2, "Lesson description 2", "Lesson image", 4, "Lesson content", "Lesson author", "Lesson group"));
//        this.lessons = list;

        // make the code pause a bit cuz the request is async if need be
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LessonAdapter lesson_adapter = new LessonAdapter(this.lessons,LessonsActivity.this);
        listLessons.setAdapter(lesson_adapter);

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
                                list.add(new Lesson(name, id, description, image, difficulty, content, author_firstname + " " + author_lastname, "Group 1"));
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
                params.put("Token", "TTGMJe1gEaCGgcq5qtHxoUyulzIvkKhBloPP9HwOey3gpDeZnGeYBKCGbJUd");
                return params;
            }
        };
        rq.add(query);
        return list;
    }
}