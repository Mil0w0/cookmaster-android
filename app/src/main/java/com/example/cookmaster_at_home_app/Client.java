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

    private Subscription client_subscription;
    public Client(int id, String isblocked, Subscription subscription, String role, String email) {
        this.id = id;
        this.isblocked = isblocked;
        this.role = role;
        this.email = email;
        this.client_subscription = subscription;
    }

    public Client(String isblocked, int id, String email, int language, String profilepicture, String firstname, String lastname, Subscription client_subscription) {
        this.isblocked = isblocked;
        this.id = id;
        this.email = email;
        this.language = language;
        this.profilepicture = profilepicture;
        this.firstname = firstname;
        this.lastname = lastname;
        this.client_subscription = client_subscription;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return firstname + " " + lastname;
    }

    public Subscription getSubscription() {
        return client_subscription;
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

    @Override
    public String toString() {
        return "Client{" +
                "email='" + email + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                '}';
    }
}
