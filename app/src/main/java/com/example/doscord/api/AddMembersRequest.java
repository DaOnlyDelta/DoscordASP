package com.example.doscord.api;

import java.util.List;

public class AddMembersRequest {

    private int channelId;

    private List<Integer> newUserIds;

    private int inviterId;

    // Update constructor
    public AddMembersRequest(int channelId, List<Integer> newUserIds, int inviterId) {
        this.channelId = channelId;
        this.newUserIds = newUserIds;
        this.inviterId = inviterId;
    }

    // Getters and Setters
    public int getChannelId() { return channelId; }
    public void setChannelId(int channelId) { this.channelId = channelId; }

    public List<Integer> getNewUserIds() { return newUserIds; }
    public void setNewUserIds(List<Integer> newUserIds) { this.newUserIds = newUserIds; }
}