package com.example.doscord.utils;

import android.app.Activity;
import android.graphics.Color;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.doscord.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Helpers {
    public static boolean switchEye(boolean currentlyVisible, EditText input, ImageButton eye) {
        // Save the font before changing the input type
        android.graphics.Typeface originalTypeface = input.getTypeface();

        if (currentlyVisible) {
            // Hide password
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            eye.setImageResource(R.drawable.eye_closed);
        } else {
            // Show password
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            eye.setImageResource(R.drawable.eye_open);
        }

        // Re-apply the font and move cursor
        input.setTypeface(originalTypeface);
        input.setSelection(input.getText().length());
        return !currentlyVisible;
    }

    public static void resetUI(Activity activity, Button btn, EditText input1, EditText input2) {
        btn.setTextColor(Color.WHITE);
        btn.setEnabled(true);
        activity.findViewById(R.id.backBtn).setEnabled(true);
        activity.findViewById(R.id.lottieAnim).setVisibility(View.GONE);
        if (input1 != null) {
            input1.setEnabled(true);
        }
        if (input2 != null) {
            input2.setEnabled(true);
        }
    }

    public static void startDotsAnimation(Activity activity, Button btn, EditText input1, EditText input2) {
        closeKeyboard(activity);
        activity.findViewById(R.id.backBtn).setEnabled(false);
        btn.setEnabled(false);
        btn.setTextColor(Color.TRANSPARENT);
        activity.findViewById(R.id.lottieAnim).setVisibility(View.VISIBLE);
        if (input1 != null) {
            input1.setEnabled(false);
        }
        if (input2 != null) {
            input2.setEnabled(false);
        }
    }

    public static void closeKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            // Use the activity parameter to access the system service
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)
                    activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE);

            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static int checkPasswordStrength(String password) {
        int points = 0;
        if (password.length() >= 6) {
            // 1. Length Check (The most important one)
            if (password.length() >= 8) points++;
            if (password.length() >= 10) points++;
            if (password.length() >= 12) points++;

            // 2. Uppercase & Lowercase Check
            if (password.matches(".*[a-z].*") && password.matches(".*[A-Z].*")) {
                points++;
            }

            // 3. Number Check
            if (password.matches(".*\\d.*")) {
                points++;
            }

            // 4. Special Character Check
            if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
                points++;
            }
        }
        return points;
    }

    public static void setDate(EditText input) {
        String selectedDate = RegDataHolder.day + "/" + (RegDataHolder.month + 1) + "/" + RegDataHolder.year;
        input.setText(selectedDate);
    }

    public static String formatTime(String rawTime) {
        if (rawTime == null || rawTime.isEmpty()) return "";

        try {
            // 1. Parse the DB string (Adjust format if your DB sends T or .000Z)
            // Since you used dateStrings: true, it should be yyyy-MM-dd HH:mm:ss
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date messageDate = sdf.parse(rawTime);
            assert messageDate != null;
            long messageTime = messageDate.getTime();
            long now = System.currentTimeMillis();

            // 2. Calculate difference in seconds
            long diffSeconds = (now - messageTime) / 1000;

            if (diffSeconds < 60) return "1m"; // Discord starts at 1m

            long diffMinutes = diffSeconds / 60;
            if (diffMinutes < 60) return diffMinutes + "m";

            long diffHours = diffMinutes / 60;
            if (diffHours < 24) return diffHours + "h";

            long diffDays = diffHours / 24;
            if (diffDays < 30) return diffDays + "d";

            long diffMonths = diffDays / 30;
            if (diffMonths < 12) return diffMonths + "mo";

            long diffYears = diffMonths / 12;
            return diffYears + "y";

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static long dateStringToMillis(String dateString) {
        if (dateString == null || dateString.isEmpty()) return 0;

        try {
            // Match the MariaDB datetime format
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            // If your Pi 4 stores time in UTC (standard for servers), uncomment the next line:
            // sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = sdf.parse(dateString);
            return (date != null) ? date.getTime() : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
