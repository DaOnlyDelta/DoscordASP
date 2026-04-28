package com.example.doscord;

public class LoginRequest {
    String identifier;
    String password;

    public LoginRequest(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }
}