package com.example.doscord.api;

public class EditMessageRequest {
    private int message_id;
    private String new_text;

    public EditMessageRequest(int messageId, String newText) {
        this.message_id = messageId;
        this.new_text = newText;
    }

    public int getMessageId() { return message_id; }
    public String getNewText() { return new_text; }
}
