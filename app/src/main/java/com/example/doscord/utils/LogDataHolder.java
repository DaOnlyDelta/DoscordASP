package com.example.doscord.utils;

public class LogDataHolder {
    public static String identifier = "";
    public static String password = "";
    public static boolean error = false;
    private static int id = -1;
    private static String username = "";
    private static String displayName = "";
    private static String pfp = "";

    // Setter
    public static void setResponseData(int id1, String username1, String displayName1, String pfp1) {
        id = id1;
        username = username1;
        displayName = displayName1;
        pfp = pfp1;
    }

    // Getters
    public static int getId() { return id; }
    public static String getUsername() { return username; }
    public static String getDisplayName() { return displayName; }
    public static String getPfp() { return pfp; }
}
