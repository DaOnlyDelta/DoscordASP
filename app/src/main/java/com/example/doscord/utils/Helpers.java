package com.example.doscord.utils;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.doscord.R;

public class Helpers {
    public static void smoothLayout(Activity activity) {
        ((ViewGroup) activity.findViewById(R.id.crPfpImg)).getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);
    }

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

    public static void startDotsAnimation(Activity activity, Button btn) {
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
}
