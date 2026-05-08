package com.example.doscord.api;

public class FriendRequestRequest {
    private int userId;
    private String friendUsername;

    public FriendRequestRequest(int userId, String friendUsername) {
        this.userId = userId;
        this.friendUsername = friendUsername;
    }

    public int getUserId() {
        return userId;
    }

    public String getFriendUsername() {
        return friendUsername;
    }
}
