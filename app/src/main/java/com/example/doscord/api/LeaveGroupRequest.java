package com.example.doscord.api;

public class LeaveGroupRequest {
    private String token;
    private int channel_id;

    public LeaveGroupRequest(String token, int channel_id) {
        this.token = token;
        this.channel_id = channel_id;
    }
}
