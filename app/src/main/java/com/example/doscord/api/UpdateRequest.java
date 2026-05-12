package com.example.doscord.api;

public class UpdateRequest {
    private String token;
    private long last_sync;

    public UpdateRequest(String token, long lastSync) {
        this.token = token;
        this.last_sync = lastSync;
    }
}