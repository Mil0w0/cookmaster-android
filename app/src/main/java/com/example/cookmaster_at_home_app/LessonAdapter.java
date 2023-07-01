package com.example.cookmaster_at_home_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class LessonAdapter extends BaseAdapter {
    private List<Lesson> list;
    private Context context;

    public LessonAdapter(List<Lesson> list, Context context) {
        this.list = list;
        this.context = context;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.lesson_row, null);
        }

        TextView lesson_name = convertView.findViewById(R.id.lesson_name);
        TextView lesson_description = convertView.findViewById(R.id.lesson_description);
        TextView lesson_author = convertView.findViewById(R.id.lesson_author);
        TextView lesson_difficulty = convertView.findViewById(R.id.difficulty_number);

        Lesson current = (Lesson) getItem(position);

        lesson_name.setText(current.getName());
        lesson_description.setText("Description: "+current.getDescription());
        lesson_author.setText("by " + current.getAuthor());
        lesson_difficulty.setText(Integer.toString(current.getDifficulty()));


        return convertView;
    }
}
