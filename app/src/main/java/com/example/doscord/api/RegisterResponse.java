package com.example.doscord.api;

/*
Error Code,Meaning,Android Action
101,Username Taken,"Set regFinalWarning to ""Username is already in use."""
102,Email/Phone Taken,"Set regFinalWarning to ""This email or phone is already registered."""
103,Weak Password,"Set regFinalWarning to ""Password must be at least 8 characters."""
999,Unknown/Server,"Set regFinalWarning to ""Something went wrong. Try again later."""
*/

public class RegisterResponse {
    private int responseCode;

    // Constructor
    public RegisterResponse(boolean success, int responseCode) {
        this.responseCode = responseCode;
    }

    // Getters
    public int getErrorCode() { return responseCode; }
}