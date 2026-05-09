package com.example.doscord.utils;

import android.net.Uri;

public class RegDataHolder {
    public static int id = -1;
    public static String email = "";
    public static String phone = "";
    public static int focused = 1; // 1 => phone, 2 => email
    public static String displayName = "";
    public static String username = "";
    public static boolean isValid = false;
    public static String password = "";
    public static boolean passStrength = false;
    public static int errorCode = 0;
    public static Integer year = null, month = null, day = null;
    public static int defaultPfpDrawable = -1;
    public static Uri selectedImageUri = null;
    public static boolean registered = false;

    public static void clear() {
        id = -1;
        email = "";
        phone = "";
        focused = 1;
        displayName = "";
        username = "";
        isValid = false;
        password = "";
        passStrength = false;
        errorCode = 0;
        year = null; month = null; day = null;
        defaultPfpDrawable = -1;
        selectedImageUri = null;
        registered = false;
    }
}
