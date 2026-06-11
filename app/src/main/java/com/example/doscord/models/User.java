package com.example.doscord.models;

public class User {
    private Integer id;
    private String username;
    private String display_name;
    private String email;
    private String phone;
    private String pfp;
    private int is_online;
    private String friends_since;
    private String created_at;

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getDisplayName() { return display_name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPfp() { return pfp; }
    public boolean isOnline() { return is_online == 1; }
    public String getFriendsSince() { return friends_since; }
    public String getCreated_at() { return created_at; }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDisplayName(String displayName) {
        this.display_name = displayName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPfp(String pfp) {
        this.pfp = pfp;
    }
}