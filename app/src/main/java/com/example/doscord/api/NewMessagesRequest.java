package com.example.doscord.api;

public class NewMessagesRequest {
    private int channel_id;
    private int after_id;

    public NewMessagesRequest(int channel_id, int after_id) {
        this.channel_id = channel_id;
        this.after_id = after_id;
    }
}