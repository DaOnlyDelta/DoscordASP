package com.example.doscord.api;

import java.util.HashMap;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class MessageRequest {
    private String channelId;
    private String senderId;
    private String messageText;

    public MessageRequest(int channelId, int senderId, int receiverId, String messageText) {
        this.channelId = String.valueOf(channelId);
        this.senderId = String.valueOf(senderId);
        this.messageText = messageText;
    }

    // Converts fields to Parts for the Multipart request
    public Map<String, RequestBody> toPartMap() {
        Map<String, RequestBody> map = new HashMap<>();
        map.put("channel_id", RequestBody.create(channelId, MediaType.parse("text/plain")));
        map.put("sender_id", RequestBody.create(senderId, MediaType.parse("text/plain")));
        map.put("message_text", RequestBody.create(messageText, MediaType.parse("text/plain")));
        return map;
    }

    // Getters
    public String getChannelId() {
        return channelId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessageText() {
        return messageText;
    }
}