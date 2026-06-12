package com.example.doscord.api;

public class DeleteMessageRequest {
    private int message_id;

    public DeleteMessageRequest(int messageId) {
        this.message_id = messageId;
    }

    public int getMessageId() { return message_id; }
}
