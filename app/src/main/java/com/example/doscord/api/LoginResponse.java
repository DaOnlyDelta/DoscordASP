package com.example.doscord.api;

public class LoginResponse {
    public User user;

    public static class User {
        private String username;
        private String display_name;
        private String pfp;

        // Getters
        public String getUsername() { return username; }
        public String getDisplayName() { return display_name; }
        public String getPfp() { return pfp; }
    }
}