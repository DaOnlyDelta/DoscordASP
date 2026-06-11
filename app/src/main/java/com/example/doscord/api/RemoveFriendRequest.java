package com.example.doscord.api;

public class RemoveFriendRequest {
    private int userId;
    private int friendId;

    public RemoveFriendRequest(int userId, int friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }
}
