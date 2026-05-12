package com.example.doscord.utils;

import com.example.doscord.api.TokenLoginResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GlobalData {
    private static Integer activeUserId;
    private static List<User> userList = new ArrayList<>();
    private static List<Integer> pendingRequestIds;

    // Fill the static data
    public static void updateData(TokenLoginResponse response) {
        userList.clear();
        List<User> list = response.getUserList();

        list.sort((u1, u2) -> {

            // Put your own profile at the top
            boolean u1IsProfile = u1.getId() == response.getActiveUserId();
            boolean u2IsProfile = u2.getId() == response.getActiveUserId();

            if (u1IsProfile && !u2IsProfile) return -1;
            if (!u1IsProfile && u2IsProfile) return 1;

            // Handle null timestamps safely
            String s1 = u1.getLastMessageTime();
            String s2 = u2.getLastMessageTime();

            long t1 = (s1 != null) ? Helpers.dateStringToMillis(s1) : 0;
            long t2 = (s2 != null) ? Helpers.dateStringToMillis(s2) : 0;

            // Descending order
            return Long.compare(t2, t1);
        });

        userList = list;

        activeUserId = response.getActiveUserId();
        pendingRequestIds = response.getPendingRequests();
    }

    public static List<User> getUserList() {
        return userList;
    }

    public static Integer getActiveUserId() {
        return activeUserId;
    }

    // Helper: Get the "Me" user object specifically
    public static User getMe() {
        for (User u : userList) {
            if (u.getId() == activeUserId) return u;
        }
        return null;
    }

    public static List<Integer> getPendingRequestIds() {
        return pendingRequestIds != null ? pendingRequestIds : new ArrayList<>();
    }

    // Helper to get actual User objects for those pending IDs
    public static List<User> getPendingUserObjects() {
        List<User> pendingUsers = new ArrayList<>();
        if (userList == null || pendingRequestIds == null) return pendingUsers;

        for (User user : userList) {
            if (pendingRequestIds.contains(user.getId())) {
                pendingUsers.add(user);
            }
        }
        return pendingUsers;
    }

    public static void removePending(int idToRemove) {
        if (pendingRequestIds != null) {
            pendingRequestIds.removeIf(id -> id == idToRemove);
        }

        // Move the user to the top of the list so they appear first in the main view
        User foundUser = null;
        for (User u : userList) {
            if (u.getId() == idToRemove) {
                foundUser = u;
                break;
            }
        }

        if (foundUser != null) {
            userList.remove(foundUser);
            userList.add(0, foundUser);
        }
    }

    public static void clear() {
        activeUserId = null; userList.clear();
    }
}
