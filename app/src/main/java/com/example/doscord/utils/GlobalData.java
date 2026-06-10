package com.example.doscord.utils; // Keep it here or move it to a .repository package if you want

import com.example.doscord.api.TokenLoginResponse;
import com.example.doscord.models.Channel;
import com.example.doscord.models.User;
import java.util.ArrayList;
import java.util.List;

public class GlobalData {
    private static Integer activeUserId;
    private static User myProfile;
    private static List<Channel> channelList = new ArrayList<>();
    private static List<User> pendingRequests = new ArrayList<>();
    private static List<User> friendList = new ArrayList<>();

    // Fill and sort the static data
    public static void updateData(TokenLoginResponse response) {
        activeUserId = response.getActiveUserId();
        myProfile = response.getProfile();
        pendingRequests = response.getPendingRequests();
        friendList = response.getFriends();

        channelList.clear();
        List<Channel> incomingChannels = response.getChannels();

        if (incomingChannels != null) {
            // Sort channels so that the most recently active ones appear at the top
            incomingChannels.sort((c1, c2) -> {
                String s1 = c1.getLastMessageTime();
                String s2 = c2.getLastMessageTime();

                long t1 = (s1 != null) ? Helpers.dateStringToMillis(s1) : 0;
                long t2 = (s2 != null) ? Helpers.dateStringToMillis(s2) : 0;

                // Descending order (newest messages first)
                return Long.compare(t2, t1);
            });
            channelList = incomingChannels;
        }
    }

    public static User findUserById(int userId) {
        if (activeUserId != null && activeUserId == userId) {
            return myProfile;
        }

        // 1. Search in friends list
        if (friendList != null) {
            for (User user : friendList) {
                if (user.getId() == userId) {
                    return user;
                }
            }
        }

        // 2. Search in pending requests
        if (pendingRequests != null) {
            for (User user : pendingRequests) {
                if (user.getId() == userId) {
                    return user;
                }
            }
        }

        // 3. Search inside active DM channels for cached recipient metadata
        if (channelList != null) {
            for (Channel channel : channelList) {
                if (!channel.isGroup() && channel.getDmRecipientId() != null && channel.getDmRecipientId() == userId) {
                    // Reconstruct a user model out of the channel's DM metadata cache
                    User cachedUser = new User();
                    cachedUser.setId(channel.getDmRecipientId());
                    cachedUser.setUsername(channel.getDmRecipientUsername());
                    cachedUser.setDisplayName(channel.getDmRecipientDisplayName());
                    cachedUser.setPfp(channel.getDmRecipientPfp());
                    return cachedUser;
                }
            }
        }

        return null;
    }

    public static Integer getActiveUserId() {
        return activeUserId;
    }

    public static User getMyProfile() {
        return myProfile;
    }

    public static List<Channel> getChannelList() {
        return channelList;
    }

    public static List<User> getPendingRequests() {
        return pendingRequests != null ? pendingRequests : new ArrayList<>();
    }

    public static List<User> getFriends() {
        return friendList != null ? friendList : new ArrayList<>();
    }

    public static void removePending(int idToRemove) {
        if (pendingRequests != null) {
            pendingRequests.removeIf(user -> user.getId() == idToRemove);
        }
    }

    public static void clear() {
        activeUserId = null;
        myProfile = null;
        channelList.clear();
        pendingRequests.clear();
        friendList.clear();
    }
}