package com.example.doscord;

public class LoginRequest {
    String identifier; // This matches the 'identifier' in our Node.js logic
    String password;

    public LoginRequest(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }
}