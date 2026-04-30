package com.example.doscord.api;

/*
Error Code,Meaning,Android Action
102,Email/Phone Taken,"Set regFinalWarning to ""This email or phone is already registered."""
999,Unknown/Server,"Set regFinalWarning to ""Something went wrong. Try again later."""
*/

public class RegisterResponse {
    private Integer errorCode;

    // Getters
    public int getErrorCode() { return errorCode; }
}