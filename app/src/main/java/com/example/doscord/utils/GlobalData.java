package com.example.doscord.utils;

import com.example.doscord.api.TokenLoginResponse;
import java.util.ArrayList;
import java.util.List;

public class GlobalData {
    private static Integer activeUserId;
    private static List<TokenLoginResponse.User> userList = new ArrayList<>();

    // Fill the static data
    public static void updateData(TokenLoginResponse response) {
        activeUserId = response.getActiveUserId();
        userList.clear();
        userList.addAll(response.getUserList());
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

    public static void clear() {
        activeUserId = null; userList.clear();
    }
}