package com.example.doscord.api;

import java.util.List;

public class TokenLoginResponse {
    private int errorCode;
    private int activeUserId;
    private List<User> userList;

    public int getErrorCode() { return errorCode; }
    public int getActiveUserId() { return activeUserId; }
    public List<User> getUserList() { return userList; }

    public static class User {
        private int id;
        private String username;
        private String display_name;
        private String pfp;
        private String created_at;    // Added
        private String friends_since; // Added (null for self)
        private String nickname;      // Added (null for self)

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getDisplayName() { return display_name; }
        public String getPfp() { return pfp; }
        public String getCreatedAt() { return created_at; }
        public String getFriendsSince() { return friends_since; }
        public String getNickname() { return nickname; }
    }
}