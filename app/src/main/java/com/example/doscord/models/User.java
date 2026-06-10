package com.example.doscord.models;

public class User {
    private Integer id;
    private String username;
    private String display_name;
    private String pfp;
    private int is_online;
    private String friends_since;

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getDisplayName() { return display_name; }
    public String getPfp() { return pfp; }
    public boolean isOnline() { return is_online == 1; }
    public String getFriendsSince() { return friends_since; }

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

    public void setPfp(String pfp) {
        this.pfp = pfp;
    }
}