package com.example.doscord.api;

public class LoginResponse {
    private User user;
    public User getUser() { return user; }

    public static class User {
        private int id;
        private String username;
        private String display_name;
        private String pfp;

        // Getters
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getDisplayName() { return display_name; }
        public String getPfp() { return pfp; }
    }
}