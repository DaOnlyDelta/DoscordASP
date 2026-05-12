package com.example.doscord.api;

import com.google.gson.annotations.SerializedName;

public class MessagesRequest {
    @SerializedName("channel_id")
    private int channelId;

    public MessagesRequest(int channelId) {
        this.channelId = channelId;
    }
}
