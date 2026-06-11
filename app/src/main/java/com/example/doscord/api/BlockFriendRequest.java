package com.example.doscord.api;

public class BlockFriendRequest {
    private int userId;
    private int friendId;

    public BlockFriendRequest(int userId, int friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }
}
