package com.example.doscord.api;

import com.example.doscord.utils.Message;
import java.util.List;

public class MessagesResponse {
    private int errorCode;
    private List<Message> messages;

    public int getErrorCode() { return errorCode; }
    public List<Message> getMessages() { return messages; }
}
