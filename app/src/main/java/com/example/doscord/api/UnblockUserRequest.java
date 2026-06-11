package com.example.doscord.api;

public class UnblockUserRequest {
    private int userId;
    private int blockedId;

    public UnblockUserRequest(int userId, int blockedId) {
        this.userId = userId;
        this.blockedId = blockedId;
    }
}
