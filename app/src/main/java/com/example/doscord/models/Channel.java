package com.example.doscord.models;

public class Channel {
    private Integer channel_id;
    private int is_group;

    private String group_name;
    private String group_pfp;

    private Integer dm_recipient_id;
    private String dm_recipient_username;
    private String dm_recipient_display_name;
    private String dm_recipient_pfp;
    private Integer dm_recipient_online;
    private String dm_recipient_nickname; // Added to catch custom nicknames

    private String last_message;
    private String last_message_time;
    private Integer last_message_sender_id;
    private String created_at;

    // Getters
    public Integer getChannelId() { return channel_id; }
    public boolean isGroup() { return is_group == 1; }

    public String getGroupName() { return group_name; }
    public String getGroupPfp() { return group_pfp; }

    public Integer getDmRecipientId() { return dm_recipient_id; }
    public String getDmRecipientUsername() { return dm_recipient_username; }
    public String getDmRecipientDisplayName() { return dm_recipient_display_name; }
    public String getDmRecipientPfp() { return dm_recipient_pfp; }
    public boolean isDmRecipientOnline() { return dm_recipient_online != null && dm_recipient_online == 1; }

    // Helper getter to prioritize custom nickname over display name and username
    public String getDmDisplayNameOrNickname() {
        if (dm_recipient_nickname != null && !dm_recipient_nickname.trim().isEmpty()) {
            return dm_recipient_nickname;
        }
        if (dm_recipient_display_name != null && !dm_recipient_display_name.trim().isEmpty()) {
            return dm_recipient_display_name;
        }
        return dm_recipient_username;
    }

    public String getDmRecipientNickname() { return dm_recipient_nickname; }
    public String getLastMessage() { return last_message; }
    public String getLastMessageTime() { return last_message_time; }
    public Integer getLastMessageSenderId() { return last_message_sender_id; }
    public String getCreatedAt() { return created_at; }
}
