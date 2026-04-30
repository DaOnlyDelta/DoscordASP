package com.example.doscord.utils;

public class RegDataHolder {
    public static String email = "";
    public static String phone = "";
    public static int focused = 1; // 1 => phone, 2 => email
    public static String displayName = "";
    public static String username = "";
    public static boolean isValid = false;
    public static String password = "";
    public static boolean passStrength = false;
    public static int errorCode = 0;

    // Helper to clear data after a successful registration
    public static void clear() {
        email = ""; phone = ""; focused = 1; displayName = ""; username = ""; isValid = false; password = ""; passStrength = false; errorCode = 0;
    }
}
