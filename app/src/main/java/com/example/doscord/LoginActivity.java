package com.example.doscord;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText passwordEditText;
    private ImageButton eyeButton;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        passwordEditText = findViewById(R.id.logPassword);
        eyeButton = findViewById(R.id.logPassEye);
    }

    public void finish(View v) { finish(); }

    public void forgotPassword(View v) {
        // Forgot password code placeholder
    }

    public void loginReq(View v) {
        // 1. UI Lockdown & References
        final Button loginBtn = findViewById(R.id.btnLogin);
        final LinearLayout loadingDots = findViewById(R.id.loadingDots);
        final EditText emailInput = findViewById(R.id.LogMailOrPhone);
        final EditText passInput = findViewById(R.id.logPassword);

        String identifier = emailInput.getText().toString().trim();
        String password = passInput.getText().toString().trim();

        if (identifier.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Lock UI & Close keyboard
        startDotsAnimation();
        loginBtn.setTextColor(android.graphics.Color.TRANSPARENT);
        loadingDots.setVisibility(View.VISIBLE);
        loginBtn.setEnabled(false);

        emailInput.setEnabled(false);
        passInput.setEnabled(false);
        closeKeyboard();

        // 3. Execute (Using the class-level apiService initialized in onCreate)
        LoginRequest loginRequest = new LoginRequest(identifier, password);

        RetrofitClient.getApiService().login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String displayName = response.body().user.display_name;
                    Toast.makeText(LoginActivity.this, "Welcome " + displayName, Toast.LENGTH_SHORT).show();
                    // Intent to HomeActivity goes here
                } else {
                    Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                    resetUI(loginBtn, emailInput, passInput);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                resetUI(loginBtn, emailInput, passInput);
                Log.e("STRESS_TEST", "Failure: " + t.getMessage());

                if (t instanceof java.net.SocketTimeoutException) {
                    Toast.makeText(LoginActivity.this, "Server is busy, try again", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startDotsAnimation() {
        final View[] dots = {
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3)
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

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void resetUI(Button btn, EditText email, EditText pass) {
        LinearLayout loadingDots = findViewById(R.id.loadingDots);
        int[] dotIds = {R.id.dot1, R.id.dot2, R.id.dot3};

        for (int id : dotIds) {
            View dot = findViewById(id);
            ObjectAnimator anim = (ObjectAnimator) dot.getTag();
            if (anim != null) {
                anim.cancel();
            }
            dot.setAlpha(1.0f); // Make sure dots are solid if they ever show up again
        }

        // Standard UI Reset
        btn.setEnabled(true);
        btn.setTextColor(android.graphics.Color.WHITE);
        loadingDots.setVisibility(View.GONE);
        email.setEnabled(true);
        pass.setEnabled(true);
    }

    public void showPass(View v) {
        if (isPasswordVisible) {
            // Hide password
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            eyeButton.setImageResource(R.drawable.eye_closed);
        } else {
            // Show password
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            eyeButton.setImageResource(R.drawable.eye_open);
        }
        passwordEditText.setSelection(passwordEditText.getText().length()); // move the cursor to the very end of the text
        isPasswordVisible = !isPasswordVisible; // flip state
    }
}