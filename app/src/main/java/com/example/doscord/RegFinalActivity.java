package com.example.doscord;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegFinalActivity extends AppCompatActivity {

    private EditText userInput, passInput;
    private ImageButton eyeBtn;
    private TextView userWarningTxt, passStrengthWarning, passWarningTxt, warningTxt;
    private boolean isPasswordVisible = false;
    private Button nextBtn;
    private final Handler debounceHandler = new Handler();
    private Runnable checkRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reg_final);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Smooth layout
        ((ViewGroup) findViewById(R.id.main)).getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);

        initViews();
        warningTransitions();
        usernameValidation();
        passwordStrength();
    }

    public void initViews() {
        userInput = findViewById(R.id.regFinalUsername);
        passInput = findViewById(R.id.regFinalPassword);
        eyeBtn = findViewById(R.id.regFinalEye);
        nextBtn = findViewById(R.id.regFinalNext);
        userWarningTxt = findViewById(R.id.regFinalUserWarning);
        passStrengthWarning = findViewById(R.id.regFinalPassWarning);
        passWarningTxt = findViewById(R.id.regFinalPassWarning2);
        warningTxt = findViewById(R.id.regFinalWarning);

        userInput.setText(RegDataHolder.username);
        passInput.setText(RegDataHolder.password);
        handleRegistrationError(RegDataHolder.errorCode);

        if (RegDataHolder.isValid) {
            userWarningTxt.setText(R.string.usernameAvailable);
            userWarningTxt.setTextColor(ContextCompat.getColor(RegFinalActivity.this, R.color.green));
        }

        if (!RegDataHolder.password.isEmpty()) {
            checkPasswordStrength(RegDataHolder.password);
        }

        checkButton();
    }

    public void warningTransitions() {
        userInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                userWarningTxt.setVisibility(View.VISIBLE);
            } else {
                // Your color logic
                int redColor = ContextCompat.getColor(RegFinalActivity.this, R.color.red);
                if (userWarningTxt.getCurrentTextColor() != redColor) {
                    userWarningTxt.setVisibility(View.GONE);
                }
            }
        });

        passInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                passWarningTxt.setVisibility(View.VISIBLE);
                // Show strength if not empty
                if (!passInput.getText().toString().trim().isEmpty()) {
                    passStrengthWarning.setVisibility(View.VISIBLE);
                }
            } else {
                int redColor = ContextCompat.getColor(RegFinalActivity.this, R.color.red);
                if (passStrengthWarning.getCurrentTextColor() != redColor) {
                    passStrengthWarning.setVisibility(View.GONE);
                }
                passWarningTxt.setVisibility(View.GONE);
            }
        });
    }

    public void showPass(View v) {
        if (isPasswordVisible) {
            // Hide password
            passInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            eyeBtn.setImageResource(R.drawable.eye_closed);
        } else {
            // Show password
            passInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            eyeBtn.setImageResource(R.drawable.eye_open);
        }
        passInput.setSelection(passInput.getText().length()); // move the cursor to the very end of the text
        isPasswordVisible = !isPasswordVisible; // flip state
    }

    public void usernameValidation() {
        userInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                RegDataHolder.isValid = false;
                nextBtn.setEnabled(false); // Disable until we verify
                nextBtn.setAlpha(0.5f);

                // Remove any pending checks because the user is still typing
                if (checkRunnable != null) {
                    debounceHandler.removeCallbacks(checkRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String name = s.toString().trim();

                if (name.isEmpty()) {
                    userWarningTxt.setText(R.string.usernameWarning);
                    userWarningTxt.setTextColor(ContextCompat.getColor(RegFinalActivity.this, R.color.gray));
                    return;
                }

                // Basic character check (Illegal characters)
                if (!name.matches("^[a-zA-Z0-9._]+$")) {
                    userWarningTxt.setText(R.string.usernameWarning);
                    userWarningTxt.setTextColor(ContextCompat.getColor(RegFinalActivity.this, R.color.red));
                    return;
                }

                checkRunnable = () -> checkUsernameOnServer(name);
                debounceHandler.postDelayed(checkRunnable, 1000); // Wait 1 second after last keystroke to check server
            }
        });
    }
    private void checkUsernameOnServer(String username) {
        if (username.length() < 2 || username.length() > 32) {
            userWarningTxt.setText(R.string.userLengthWarning);
            userWarningTxt.setTextColor(ContextCompat.getColor(RegFinalActivity.this, R.color.red));
            return;
        }

        // Show a tiny loading indicator if you want
        RetrofitClient.getApiService().checkUsername(new CheckRequest(username))
                .enqueue(new Callback<CheckResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CheckResponse> call, @NonNull Response<CheckResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().isAvailable()) {
                                RegDataHolder.isValid = true;
                                checkButton();
                                userWarningTxt.setText(R.string.usernameAvailable);
                                userWarningTxt.setTextColor(ContextCompat.getColor(RegFinalActivity.this, R.color.green));
                                if (!userInput.hasFocus()) {
                                    userWarningTxt.setVisibility(View.GONE);
                                }
                            } else {
                                userWarningTxt.setText(R.string.usernameTakenWarning);
                                userWarningTxt.setTextColor(ContextCompat.getColor(RegFinalActivity.this, R.color.red));
                                nextBtn.setEnabled(false);
                                nextBtn.setAlpha(0.5f);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CheckResponse> call, @NonNull Throwable t) {
                        // Silent fail or log
                    }
                });
    }

    public void passwordStrength() {
        passInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                RegDataHolder.passStrength = false;
                nextBtn.setEnabled(false); // Disable until we verify
                nextBtn.setAlpha(0.5f);
                String pass = s.toString();

                if (pass.isEmpty()) {
                    passStrengthWarning.setVisibility(View.GONE);
                    return;
                }

                passStrengthWarning.setVisibility(View.VISIBLE);
                checkPasswordStrength(pass);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void checkPasswordStrength(String password) {
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
            if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
                points++;
            }
        }

        // --- UPDATING THE UI ---
        if (points < 2) {
            passStrengthWarning.setText(R.string.passWeak);
            passStrengthWarning.setTextColor(ContextCompat.getColor(RegFinalActivity.this, R.color.red));
            RegDataHolder.passStrength = false;
        } else if (points <= 4) {
            passStrengthWarning.setText(R.string.passMedium);
            passStrengthWarning.setTextColor(ContextCompat.getColor(RegFinalActivity.this, R.color.yellow));
            RegDataHolder.passStrength = true;
            checkButton();
        } else {
            passStrengthWarning.setText(R.string.passStrong);
            passStrengthWarning.setTextColor(ContextCompat.getColor(RegFinalActivity.this, R.color.green));
            RegDataHolder.passStrength = true;
            checkButton();
        }
    }

    public void checkButton() {
        if (RegDataHolder.isValid && RegDataHolder.passStrength) {
            nextBtn.setEnabled(true);
            nextBtn.setAlpha(1.0f);
        } else {
            nextBtn.setEnabled(false);
            nextBtn.setAlpha(0.5f);
        }
    }

    public void showPass() {
        eyeBtn.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Switch to Hidden
                passInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeBtn.setImageResource(R.drawable.eye_closed);
            } else {
                // Switch to Visible
                passInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eyeBtn.setImageResource(R.drawable.eye_open);
            }

            isPasswordVisible = !isPasswordVisible;

            // Keep the cursor at the end of the text
            passInput.setSelection(passInput.getText().length());
        });
    }

    public void registerReq(View v) {
        RegDataHolder.errorCode = 0;
        // Get inputs
        RegDataHolder.username = userInput.getText().toString().trim();
        RegDataHolder.password = passInput.getText().toString();

        // Set up UI for loading state
        closeKeyboard();
        nextBtn.setEnabled(false);
        nextBtn.setTextColor(android.graphics.Color.TRANSPARENT);
        userInput.setEnabled(false);
        passInput.setEnabled(false);
        findViewById(R.id.loadingDots).setVisibility(View.VISIBLE);
        startDotsAnimation();

        // Create the request object
        RegisterRequest request = new RegisterRequest();

        // Send to Pi
        RetrofitClient.getApiService().register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                resetUI(nextBtn, userInput, passInput);

                if (response.isSuccessful()) {
                    handleRegistrationError(1);
                } else {
                    // Handle Error Codes
                    try {
                        // Retrofit won't automatically parse the body on a 400 error,
                        // so we manually convert the errorBody to our object
                        RegisterResponse errorRes = new Gson().fromJson(response.errorBody().charStream(), RegisterResponse.class);
                        handleRegistrationError(errorRes.getErrorCode());
                    } catch (Exception e) {
                        handleRegistrationError(999); // Generic error
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                warningTxt.setText("Server Error! Restart app and try again!");
            }
        });
    }

    private void handleRegistrationError(int code) {
        RegDataHolder.errorCode = code;
        warningTxt.setVisibility(View.VISIBLE);

        switch (code) {
            case 1:
                warningTxt.setText(R.string.regSuccessful);
                warningTxt.setTextColor(ContextCompat.getColor(RegFinalActivity.this, R.color.green));
                warningTxt.postDelayed(this::finish, 1000);
                break;
            case 102:
                finish(); // This kills the "Final" screen and shows the "Email/Phone" screen
                break;
            case 999:
                warningTxt.setText("Generic server Error!");
                break;
        }
    }

    private void startDotsAnimation() {
        final View[] dots = {
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3)
        };

        for (int i = 0; i < dots.length; i++) {
            final View dot = dots[i];
            dot.setAlpha(0.3f);

            ObjectAnimator animator = ObjectAnimator.ofFloat(dot, "alpha", 0.3f, 1f, 0.3f);
            animator.setDuration(800); // Set to 800 for Discord-like speed
            animator.setStartDelay(i * 160);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.RESTART);

            dot.setTag(animator);
            animator.start();
        }
    }

    private void resetUI(Button btn, EditText user, EditText pass) {
        LinearLayout loadingDots = findViewById(R.id.loadingDots);
        int[] dotIds = {R.id.dot1, R.id.dot2, R.id.dot3};

        for (int id : dotIds) {
            View dot = findViewById(id);
            ObjectAnimator anim = (ObjectAnimator) dot.getTag();
            if (anim != null) {
                anim.cancel();
            }
            dot.setAlpha(1.0f);
        }

        // Standard UI Reset
        btn.setEnabled(true);
        btn.setTextColor(android.graphics.Color.WHITE);
        loadingDots.setVisibility(View.GONE);
        user.setEnabled(true);
        pass.setEnabled(true);
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void finish(View v) {
        RegDataHolder.username = userInput.getText().toString().trim();
        RegDataHolder.password = passInput.getText().toString();
        finish();
    }
}