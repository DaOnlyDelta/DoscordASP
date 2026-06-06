package com.example.doscord.api;

import com.example.doscord.models.Channel;
import com.example.doscord.models.User;
import java.util.List;

public class TokenLoginResponse {
    private Integer errorCode;
    private Integer activeUserId;
    private User profile;
    private List<Channel> channels;
    private List<User> pendingRequests;
    private long sync_timestamp;

    public Integer getErrorCode() { return errorCode; }
    public Integer getActiveUserId() { return activeUserId; }
    public User getProfile() { return profile; }
    public List<Channel> getChannels() { return channels; }
    public List<User> getPendingRequests() { return pendingRequests; } // Changed return type
    public long getSyncTimestamp() { return sync_timestamp; }
}