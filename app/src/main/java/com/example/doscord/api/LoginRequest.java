package com.example.doscord.api;

public class LoginRequest {
    private String identifier;
    private String password;
    private String deviceName;

    public LoginRequest(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
        this.deviceName = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
    }
}