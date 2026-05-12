package com.example.doscord.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

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

    public static void resetUI(Activity activity, Button btn, ImageButton backBtn, EditText input1, EditText input2) {
        btn.setTextColor(android.graphics.Color.WHITE);
        btn.setEnabled(true);
        backBtn.setEnabled(true);

        LinearLayout loadingDots = activity.findViewById(R.id.loadingDots);
        final View[] dots = {
                activity.findViewById(R.id.dot1),
                activity.findViewById(R.id.dot2),
                activity.findViewById(R.id.dot3)
        };

        for (View dot : dots) {
            ObjectAnimator anim = (ObjectAnimator) dot.getTag();
            if (anim != null) {
                anim.cancel();
            }
            dot.setAlpha(1.0f); // Make sure dots are solid if they ever show up again
        }

        // Standard UI Reset
        loadingDots.setVisibility(View.GONE);
        if (input1 != null) {
            input1.setEnabled(true);
        }
        if (input2 != null) {
            input2.setEnabled(true);
        }
    }

    public static void startDotsAnimation(Activity activity, Button btn, ImageButton back) {
        closeKeyboard(activity);
        back.setEnabled(false);
        btn.setTextColor(android.graphics.Color.TRANSPARENT);
        btn.setEnabled(false);

        activity.findViewById(R.id.loadingDots).setVisibility(View.VISIBLE);
        final View[] dots = {
                activity.findViewById(R.id.dot1),
                activity.findViewById(R.id.dot2),
                activity.findViewById(R.id.dot3)
        };

        for (int i = 0; i < dots.length; i++) {
            final View dot = dots[i];

            // Start at 30% opacity
            dot.setAlpha(0.3f);

            // Animate from 0.3 (dim) to 1.0 (bright) and back
            ObjectAnimator animator = ObjectAnimator.ofFloat(dot, "alpha", 0.3f, 1f, 0.3f);
            animator.setDuration(2000);
            animator.setStartDelay(i * 500);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.RESTART);

            dot.setTag(animator);
            animator.start();
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
