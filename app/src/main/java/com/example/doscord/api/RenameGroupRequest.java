package com.example.doscord.api;

public class RenameGroupRequest {
    private int userId;
    private int channel_id;
    private String new_name;

    public RenameGroupRequest(int userId, int channel_id, String new_name) {
        this.userId = userId;
        this.channel_id = channel_id;
        this.new_name = new_name;
    }
}
