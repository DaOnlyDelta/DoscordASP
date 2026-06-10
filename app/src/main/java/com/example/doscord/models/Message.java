package com.example.doscord.models;

public class Message {
    private Integer id;
    private Integer sender_id;
    private Integer channel_id;
    private String message_text;
    private String sent_at;
    private String type; // Will be "normal" or "system"

    public Message(Integer senderId, String content, String sentAt) {
        this.sender_id = senderId;
        this.message_text = content;
        this.sent_at = sentAt;
    }

    // Getters
    public Integer getId() { return id; }
    public Integer getSenderId() { return sender_id; }
    public Integer getChannelId() { return channel_id; }
    public String getMessageText() { return message_text; }
    public String getSentAt() { return sent_at; }
    public String getType() {
        return type != null ? type : "normal";
    }
}
