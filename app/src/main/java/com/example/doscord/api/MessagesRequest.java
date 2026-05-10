package com.example.doscord.api;

import com.google.gson.annotations.SerializedName;

public class MessagesRequest {
    private String token;
    @SerializedName("channel_id")
    private int channelId;
    private int limit;

    public MessagesRequest(String token, int channelId, int limit) {
        this.token = token;
        this.channelId = channelId;
        this.limit = limit;
    }
}
