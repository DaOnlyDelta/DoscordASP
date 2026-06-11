package com.example.doscord.api;

public class UpdateNicknameRequest {
    private int userId;
    private int friendId;
    private String nickname;

    public UpdateNicknameRequest(int userId, int friendId, String nickname) {
        this.userId = userId;
        this.friendId = friendId;
        this.nickname = nickname;
    }
}