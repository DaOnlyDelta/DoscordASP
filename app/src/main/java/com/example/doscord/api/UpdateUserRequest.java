package com.example.doscord.api;

public class UpdateUserRequest {
    private String token;
    private String displayName;
    private String email;
    private String phone;

    public UpdateUserRequest(String token, String displayName, String email, String phone) {
        this.token = token;
        this.displayName = displayName;
        this.email = email;
        this.phone = phone;
    }
}