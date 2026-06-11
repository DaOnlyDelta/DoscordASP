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

        List<Channel> incomingChannels = response.getChannels();
        if (incomingChannels != null) {
            List<Channel> filteredChannels = new ArrayList<>();
            for (Channel channel : incomingChannels) {
                if (channel.isGroup()) {
                    filteredChannels.add(channel);
                } else {
                    // For DMs, only include if the recipient is in the friend list
                    boolean isFriend = false;
                    if (friendList != null) {
                        for (User friend : friendList) {
                            if (friend.getId() == channel.getDmRecipientId()) {
                                isFriend = true;
                                break;
                            }
                        }
                    }
                    if (isFriend) {
                        filteredChannels.add(channel);
                        // Sync nickname from channel payload to friend user object for consistent display elsewhere
                        for (User friend : friendList) {
                            if (friend.getId() == channel.getDmRecipientId()) {
                                friend.setNickname(channel.getDmRecipientNickname());
                                break;
                            }
                        }
                    }
                }
            }

            // Sort filtered channels based on activity or creation time
            filteredChannels.sort((c1, c2) -> {
                String s1 = c1.getLastMessageTime();
                String s2 = c2.getLastMessageTime();

                // If s1 or s2 are "null" as string, treat as null
                if ("null".equals(s1)) s1 = null;
                if ("null".equals(s2)) s2 = null;

                long t1 = (s1 != null && !s1.isEmpty()) ? Helpers.dateStringToMillis(s1) : 0;
                long t2 = (s2 != null && !s2.isEmpty()) ? Helpers.dateStringToMillis(s2) : 0;

                // Fallback to channel creation time if no messages exist
                if (t1 == 0) t1 = Helpers.dateStringToMillis(c1.getCreatedAt());
                if (t2 == 0) t2 = Helpers.dateStringToMillis(c2.getCreatedAt());

                if (t1 == t2) {
                    // Final tie-breaker
                    return Integer.compare(c2.getChannelId(), c1.getChannelId());
                }

                // Descending order (newest activity/creation first)
                return Long.compare(t2, t1);
            });
            channelList = filteredChannels;
        } else {
            channelList.clear();
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