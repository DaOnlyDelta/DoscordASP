package com.example.doscord;

public class LoginResponse {
    String message;
    User user;

    public class User {
        int id;
        String username;
        String display_name;
        String pfp;
    }
}