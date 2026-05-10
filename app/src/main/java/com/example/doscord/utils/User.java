package com.example.doscord.utils;

import com.google.gson.annotations.SerializedName;

public class User {
    private Integer id;
    private String username;
    @SerializedName("display_name")
    private String displayName;
    private String pfp;
    @SerializedName("channel_id")
    private Integer channelId;
    private String nickname;
    @SerializedName("friends_since")
    private String friendsSince;
    @SerializedName("last_message")
    private String lastMessage;
    @SerializedName("last_message_time")
    private String lastMessageTime;
    @SerializedName("last_message_sender_id")
    private Integer lastMessageSenderId;

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public String getPfp() { return pfp; }
    public Integer getChannelId() { return channelId; }
    public String getNickname() { return nickname; }
    public String getFriendsSince() { return friendsSince; }
    public String getLastMessage() { return lastMessage; }
    public String getLastMessageTime() { return lastMessageTime; }
    public Integer getLastMessageSenderId() { return lastMessageSenderId; }
}
