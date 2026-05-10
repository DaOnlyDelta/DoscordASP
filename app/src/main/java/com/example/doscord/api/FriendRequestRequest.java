package com.example.doscord.api;

public class FriendRequestRequest {
    private int userId;
    private String friendUsername;

    public FriendRequestRequest(int userId, String friendUsername) {
        this.userId = userId;
        this.friendUsername = friendUsername;
    }
}
