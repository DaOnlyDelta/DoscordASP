package com.example.doscord;

/*
Error Code,Meaning,Android Action
101,Username Taken,"Set regFinalWarning to ""Username is already in use."""
102,Email/Phone Taken,"Set regFinalWarning to ""This email or phone is already registered."""
103,Weak Password,"Set regFinalWarning to ""Password must be at least 8 characters."""
999,Unknown/Server,"Set regFinalWarning to ""Something went wrong. Try again later."""
*/

public class RegisterResponse {
    private boolean success;
    private int errorCode;

    // Constructor
    public RegisterResponse(boolean success, String message, int errorCode) {
        this.success = success;
        this.errorCode = errorCode;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public int getErrorCode() { return errorCode; }
}