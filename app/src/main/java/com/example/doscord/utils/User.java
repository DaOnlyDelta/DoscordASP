package com.example.doscord.utils;

public class User {
    private int id;
    private String username;
    private String display_name;
    private String pfp;
    private String created_at;
    private String friends_since;
    private String nickname;
    private String last_message;
    private String last_message_time;
    private Integer last_message_sender_id;

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getDisplayName() { return display_name; }
    public String getPfp() { return pfp; }
    public String getCreatedAt() { return created_at; }
    public String getFriendsSince() { return friends_since; }
    public String getNickname() { return nickname; }
    public String getLastMessage() { return last_message; }
    public String getLastMessageTime() { return last_message_time; }
    public Integer getLastMessageSenderId() { return last_message_sender_id; }
}
