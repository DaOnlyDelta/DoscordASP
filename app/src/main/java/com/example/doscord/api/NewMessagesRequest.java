package com.example.doscord.api;

public class NewMessagesRequest {
    private int channel_id;
    private int after_id;
    private String token;

    public NewMessagesRequest(int channel_id, int after_id, String token) {
        this.channel_id = channel_id;
        this.after_id = after_id;
        this.token = token;
    }
}