package com.example.doscord.utils;

import com.example.doscord.api.TokenLoginResponse;
import java.util.ArrayList;
import java.util.List;

public class GlobalData {
    private static Integer activeUserId;
    private static List<TokenLoginResponse.User> userList = new ArrayList<>();
    private static List<Integer> pendingRequestIds;

    // Fill the static data
    public static void updateData(TokenLoginResponse response) {
        activeUserId = response.getActiveUserId();
        userList.clear();
        userList.addAll(response.getUserList());
        pendingRequestIds = response.getPendingRequests();
    }

    public static List<TokenLoginResponse.User> getUserList() {
        return userList;
    }

    public static Integer getActiveUserId() {
        return activeUserId;
    }

    // Helper: Get the "Me" user object specifically
    public static TokenLoginResponse.User getMe() {
        for (TokenLoginResponse.User u : userList) {
            if (u.getId() == activeUserId) return u;
        }
        return null;
    }

    public static List<Integer> getPendingRequestIds() {
        return pendingRequestIds != null ? pendingRequestIds : new ArrayList<>();
    }

    // Helper to get actual User objects for those pending IDs
    public static List<TokenLoginResponse.User> getPendingUserObjects() {
        List<TokenLoginResponse.User> pendingUsers = new ArrayList<>();
        if (userList == null || pendingRequestIds == null) return pendingUsers;

        for (TokenLoginResponse.User user : userList) {
            if (pendingRequestIds.contains(user.getId())) {
                pendingUsers.add(user);
            }
        }
        return pendingUsers;
    }

    public static void clear() {
        activeUserId = null; userList.clear();
    }
}