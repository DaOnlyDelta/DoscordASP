package com.example.doscord.api;

import com.example.doscord.utils.RegDataHolder;

import java.util.Locale;

public class RegisterRequest {
    private String email;
    private String phone;
    private String displayName;
    private String username;
    private String password;
    private String birthday;

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

        this.birthday = String.format(Locale.US, "%d-%02d-%02d",
                RegDataHolder.year,
                (RegDataHolder.month + 1),
                RegDataHolder.day);
    }
}