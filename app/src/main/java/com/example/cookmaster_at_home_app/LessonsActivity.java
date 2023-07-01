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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons);
        listLessons = findViewById(R.id.listLessons);

        //getLessons();
        List<Lesson> list =new ArrayList<>();
        list.add(new Lesson("Lesson name", 1, "Lesson description", "Lesson image", 2, "Lesson content", "Lesson author", "Lesson group"));
        list.add(new Lesson("Lesson name 2", 2, "Lesson description 2", "Lesson image", 4, "Lesson content", "Lesson author", "Lesson group"));
        this.lessons = list;

        LessonAdapter lesson_adapter = new LessonAdapter(list,LessonsActivity.this);
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
                            JSONObject json = new JSONObject(response);
                            Toast.makeText(LessonsActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                            System.out.println(response.toString());

                            JSONArray all = json.getJSONArray("lessons");
//                            for (int i = 0; i < all.length(); i++) {
//                                System.out.println(all.getJSONObject(i));
//                            }

//                        list.add(new Lesson(name, description, difficulty, content);
//                        this.lessons = list;

                        }catch (Exception e){
                            Toast.makeText(LessonsActivity.this,"ERROR1: %s".format(e.getMessage()) , Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null){
                            String errorMessage = new String(error.networkResponse.data);
                            Toast.makeText(LessonsActivity.this,errorMessage, Toast.LENGTH_SHORT).show();
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