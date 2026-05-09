package com.example.doscord.api;

import java.util.List;

public class TokenLoginResponse {
    private int errorCode;
    private int activeUserId;
    private List<User> userList;
    private List<Integer> pendingRequests;

    public int getErrorCode() { return errorCode; }
    public int getActiveUserId() { return activeUserId; }
    public List<User> getUserList() { return userList; }
    public List<Integer> getPendingRequests() { return pendingRequests; }

    public static class User {
        private int id;
        private String username;
        private String display_name;
        private String pfp;
        private String created_at;
        private String friends_since;
        private String nickname;
        private String last_message;
        private String last_message_time;

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getDisplayName() { return display_name; }
        public String getPfp() { return pfp; }
        public String getCreatedAt() { return created_at; }
        public String getFriendsSince() { return friends_since; }
        public String getNickname() { return nickname; }
        public String getLastMessage() { return last_message; }
        public String getLastMessageTime() { return last_message_time; }
    }
}