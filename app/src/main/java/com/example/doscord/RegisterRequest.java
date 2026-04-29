package com.example.doscord;

public class RegisterRequest {
    private String email;
    private String phone;
    private String displayName;
    private String username;
    private String password;

    public RegisterRequest() {
        this.username = RegDataHolder.username;
        this.password = RegDataHolder.password;
        this.displayName = RegDataHolder.displayName;

        if (RegDataHolder.focused == 2) {
            this.email = RegDataHolder.email;
            this.phone = null;
        } else {
            this.phone = RegDataHolder.phone;
            this.email = null;
        }
    }
}