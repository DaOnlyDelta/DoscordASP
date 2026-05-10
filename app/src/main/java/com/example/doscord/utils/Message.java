package com.example.doscord.utils;

import com.google.gson.annotations.SerializedName;

public class Message {
    private int id;
    @SerializedName("sender_id")
    private int senderId;
    @SerializedName("channel_id")
    private int channelId;
    private String message_text;
    @SerializedName("sent_at")
    private String sentAt;

    public Message(int senderId, String content, String sentAt) {
        this.senderId = senderId;
        this.message_text = content;
        this.sentAt = sentAt;
    }

    // Getters
    public int getId() { return id; }
    public int getSenderId() { return senderId; }
    public int getChannelId() { return channelId; }
    public String getMessageText() { return message_text; }
    public String getSentAt() { return sentAt; }
}
