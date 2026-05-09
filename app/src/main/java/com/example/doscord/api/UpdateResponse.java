package com.example.doscord.api;

import java.util.List;

public class UpdateResponse {
    private int errorCode;
    private List<FriendRequest> friendRequests;

    // Getters
    public int getErrorCode() { return errorCode; }
    public List<FriendRequest> getFriendRequests() { return friendRequests; }

    public static class FriendRequest {
        private int id;
        private String username;
        private String display_name;
        private String pfp;

        // Getters
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getDisplayName() { return display_name; }
        public String getPfp() { return pfp; }
    }
}