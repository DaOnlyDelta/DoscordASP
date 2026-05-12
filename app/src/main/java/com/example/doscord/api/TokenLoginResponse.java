package com.example.doscord.api;

import com.example.doscord.utils.User;
import java.util.List;

public class TokenLoginResponse {
    private int errorCode;
    private int activeUserId;
    private List<User> userList;
    private List<Integer> pendingRequests;
    private long sync_timestamp;

    public int getErrorCode() { return errorCode; }
    public int getActiveUserId() { return activeUserId; }
    public List<User> getUserList() { return userList; }
    public List<Integer> getPendingRequests() { return pendingRequests; }
    public long getSyncTimestamp() { return sync_timestamp; }
}