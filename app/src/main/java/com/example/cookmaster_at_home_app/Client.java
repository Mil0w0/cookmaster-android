package com.example.cookmaster_at_home_app;

public class Client {
    private String isblocked;
    private String role;
    private int id;

    private String email;
    private int language;
    private String profilepicture;
    private String firstname;
    private String lastname;
    private int subscription;
    public Client(int id, String isblocked, int subscription, String role, String email) {
        this.id = id;
        this.subscription = subscription;
        this.isblocked = isblocked;
        this.role = role;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return firstname + " " + lastname;
    }

    public int getSubscription() {
        return subscription;
    }

    public String getIsblocked() {
        return isblocked;
    }

    public String getEmail() {
        return email;
    }
    public void setIsblocked(String isblocked) {
        this.isblocked = isblocked;
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
}
