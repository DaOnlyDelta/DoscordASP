package com.example.doscord.activities.menu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doscord.api.CheckRequest;
import com.example.doscord.api.CheckResponse;
import com.example.doscord.R;
import com.example.doscord.utils.Helpers;
import com.example.doscord.utils.RegDataHolder;
import com.example.doscord.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegUserActivity extends AppCompatActivity {

    private EditText userInput, passInput;
    private ImageButton eyeBtn;
    private TextView userWarningTxt, passStrengthWarningTxt, passWarningTxt;
    private boolean isPasswordVisible = false;
    private Button nextBtn;
    private final Handler debounceHandler = new Handler();
    private Runnable checkRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reg_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.crPfpImg), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Smooth layout
        Helpers.smoothLayout(this);

        initViews();
        warningTransitions();
        usernameValidation();
        passwordStrength();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleRegistrationError(RegDataHolder.errorCode);
        if (isPasswordVisible) { // Hide password
            regSwitchEye(null);
        }
    }

    public void initViews() {
        userInput = findViewById(R.id.regUserUserInput);
        passInput = findViewById(R.id.regUserPassInput);
        eyeBtn = findViewById(R.id.regUserEyeBtn);
        nextBtn = findViewById(R.id.regNextBtn);
        userWarningTxt = findViewById(R.id.regUserUserWarning);
        passStrengthWarningTxt = findViewById(R.id.regUserPassWarning);
        passWarningTxt = findViewById(R.id.regUserPassWarning2);

        userInput.setText(RegDataHolder.username);
        passInput.setText(RegDataHolder.password);
        handleRegistrationError(RegDataHolder.errorCode);

        if (RegDataHolder.isValid) {
            userWarningTxt.setText(R.string.usernameAvailable);
            userWarningTxt.setTextColor(ContextCompat.getColor(RegUserActivity.this, R.color.green));
        }

        if (!RegDataHolder.password.isEmpty()) {
            updatePassWarning(RegDataHolder.password);
        }

        checkButton();
    }

    public void warningTransitions() {
        userInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                userWarningTxt.setVisibility(View.VISIBLE);
            } else {
                // Your color logic
                int redColor = ContextCompat.getColor(RegUserActivity.this, R.color.red);
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
                    passStrengthWarningTxt.setVisibility(View.VISIBLE);
                }
            } else {
                int redColor = ContextCompat.getColor(RegUserActivity.this, R.color.red);
                if (passStrengthWarningTxt.getCurrentTextColor() != redColor) {
                    passStrengthWarningTxt.setVisibility(View.GONE);
                }
                passWarningTxt.setVisibility(View.GONE);
            }
        });
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
                    userWarningTxt.setTextColor(ContextCompat.getColor(RegUserActivity.this, R.color.gray));
                    return;
                }

                // Basic character check (Illegal characters)
                if (!name.matches("^[a-zA-Z0-9._]+$")) {
                    userWarningTxt.setText(R.string.usernameWarning);
                    userWarningTxt.setTextColor(ContextCompat.getColor(RegUserActivity.this, R.color.red));
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
            userWarningTxt.setTextColor(ContextCompat.getColor(RegUserActivity.this, R.color.red));
            return;
        }

        RetrofitClient.getApiService().checkUsername(new CheckRequest(username))
                .enqueue(new Callback<CheckResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CheckResponse> call, @NonNull Response<CheckResponse> response) {
                        Log.d("API_DEBUG", "Code: " + response.code());
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().isAvailable()) {
                                RegDataHolder.isValid = true;
                                checkButton();
                                userWarningTxt.setText(R.string.usernameAvailable);
                                userWarningTxt.setTextColor(ContextCompat.getColor(RegUserActivity.this, R.color.green));
                                if (!userInput.hasFocus()) {
                                    userWarningTxt.setVisibility(View.GONE);
                                }
                            } else {
                                userWarningTxt.setText(R.string.usernameTakenWarning);
                                userWarningTxt.setVisibility(View.VISIBLE);
                                userWarningTxt.setTextColor(ContextCompat.getColor(RegUserActivity.this, R.color.red));
                                nextBtn.setEnabled(false);
                                nextBtn.setAlpha(0.5f);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CheckResponse> call, @NonNull Throwable t) {
                        // Silent fail
                        Log.e("API_DEBUG", "FAILED TO CONNECT: " + t.getMessage());
                        t.printStackTrace();
                    }
                });
    }

    private void passwordStrength() {
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
                    passStrengthWarningTxt.setVisibility(View.GONE);
                    return;
                }

                passStrengthWarningTxt.setVisibility(View.VISIBLE);
                updatePassWarning(pass);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updatePassWarning(String password) {
        int points = Helpers.checkPasswordStrength(password);
        if (points < 2) {
            passStrengthWarningTxt.setText(R.string.passWeak);
            passStrengthWarningTxt.setTextColor(ContextCompat.getColor(RegUserActivity.this, R.color.red));
            RegDataHolder.passStrength = false;
        } else if (points <= 4) {
            passStrengthWarningTxt.setText(R.string.passMedium);
            passStrengthWarningTxt.setTextColor(ContextCompat.getColor(RegUserActivity.this, R.color.yellow));
            RegDataHolder.passStrength = true;
            checkButton();
        } else {
            passStrengthWarningTxt.setText(R.string.passStrong);
            passStrengthWarningTxt.setTextColor(ContextCompat.getColor(RegUserActivity.this, R.color.green));
            RegDataHolder.passStrength = true;
            checkButton();
        }
    }

    private void checkButton() {
        if (RegDataHolder.isValid && RegDataHolder.passStrength) {
            nextBtn.setEnabled(true);
            nextBtn.setAlpha(1.0f);
        } else {
            nextBtn.setEnabled(false);
            nextBtn.setAlpha(0.5f);
        }
    }

    public void regSwitchEye(View v) {
        isPasswordVisible = Helpers.switchEye(isPasswordVisible, passInput, eyeBtn);
    }

    private void handleRegistrationError(int code) {
        RegDataHolder.errorCode = code;

        switch (code) {
            case 1:
            case 102:
                finish();
                break;
            case 999:
                nextBtn.setText(R.string.generic_server_error);
                break;
        }
    }

    public void openRegBdayActivity(View v) {
        RegDataHolder.username = userInput.getText().toString().trim();
        RegDataHolder.password = passInput.getText().toString();

        Intent intent = new Intent(this, RegBdayActivity.class);
        startActivity(intent);
    }

    public void finish(View v) {
        RegDataHolder.username = userInput.getText().toString().trim();
        RegDataHolder.password = passInput.getText().toString();
        finish();
    }
}