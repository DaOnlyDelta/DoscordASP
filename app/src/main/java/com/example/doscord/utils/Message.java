package com.example.doscord.utils;

import com.google.gson.annotations.SerializedName;

public class Message {
    private Integer id;
    @SerializedName("sender_id")
    private Integer senderId;
    @SerializedName("channel_id")
    private Integer channelId;
    private String message_text;
    @SerializedName("sent_at")
    private String sentAt;

    public Message(Integer senderId, String content, String sentAt) {
        this.senderId = senderId;
        this.message_text = content;
        this.sentAt = sentAt;
    }

    // Getters
    public Integer getId() { return id; }
    public Integer getSenderId() { return senderId; }
    public Integer getChannelId() { return channelId; }
    public String getMessageText() { return message_text; }
    public String getSentAt() { return sentAt; }
}
