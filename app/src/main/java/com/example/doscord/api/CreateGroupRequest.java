package com.example.doscord.api;

import java.util.List;

public class CreateGroupRequest {
    private int creatorId;
    private List<Integer> members;

    public CreateGroupRequest(int creatorId, List<Integer> members) {
        this.creatorId = creatorId;
        this.members = members;
    }
}