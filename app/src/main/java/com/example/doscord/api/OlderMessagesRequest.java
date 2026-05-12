package com.example.doscord.api;

public class OlderMessagesRequest {
    private int channel_id;
    private int before_id;

    public OlderMessagesRequest(int channel_id, int before_id) {
        this.channel_id = channel_id;
        this.before_id = before_id;
    }
}