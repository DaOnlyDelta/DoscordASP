package com.example.doscord.api;

import com.example.doscord.utils.Message;
import java.util.List;

public class MessagesResponse {
    private int errorCode;
    private List<Message> messages;
    private boolean hasMore;

    public int getErrorCode() { return errorCode; }
    public List<Message> getMessages() { return messages; }
    public boolean isHasMore() { return hasMore; }
}
