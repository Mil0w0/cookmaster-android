package com.example.cookmaster_at_home_app;

public class Lesson {
    private String name;
    private int id;
    private String description;
    private String image;
    private int difficulty;
    private String content;
    private String author;
    private int group;

    public Lesson(String name, int id, String description, String image, int difficulty, String content, String author, int group_id) {
        this.name = name;
        this.id = id;
        this.description = description;
        this.image = image;
        this.difficulty = difficulty;
        this.content = content;
        this.author = author;
        this.group = group_id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public String getAuthor() {
        return author;
    }

    public int getGroup() {
        return group;
    }

    public String getContent() {return content;}
}


