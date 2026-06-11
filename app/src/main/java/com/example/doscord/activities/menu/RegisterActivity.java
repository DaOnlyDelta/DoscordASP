package com.example.doscord.activities.menu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doscord.R;
import com.example.doscord.api.RegisterRequest;
import com.example.doscord.api.RegisterResponse;
import com.example.doscord.api.RetrofitClient;
import com.example.doscord.utils.Helpers;
import com.example.doscord.utils.RegDataHolder;
import com.example.doscord.utils.SessionManager;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private View selector;
    private FrameLayout blackBar;
    private TextView label, warningTxt, serverWarningTxt;
    private EditText input;
    private Button btnPhone, btnEmail, nextBtn;
    private boolean midRequest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        loadSavedData();
        setupSelectorWidth();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleRegistrationError(RegDataHolder.errorCode);
    }

    private void initViews() {
        selector = findViewById(R.id.regSelector);
        blackBar = findViewById(R.id.regBar);
        label = findViewById(R.id.regLabel);
        input = findViewById(R.id.regIdentifierInput);
        btnPhone = findViewById(R.id.regPhoneBtn);
        btnEmail = findViewById(R.id.regEmailBtn);
        nextBtn = findViewById(R.id.regNextBtn);
        warningTxt = findViewById(R.id.regWarning);
        serverWarningTxt = findViewById(R.id.regServerWarning);
    }

    private void loadSavedData() {
        // Check if we have an email saved
        if (!RegDataHolder.email.isEmpty() && RegDataHolder.focused == 2) {
            // Update UI to Email mode immediately (no animation needed yet)
            input.setText(RegDataHolder.email);
            input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            input.setHint(R.string.email);
            label.setText(R.string.email);

            // We need to move the selector to the right position
            blackBar.post(() -> {
                float moveX = blackBar.getWidth() / 2f;
                selector.setTranslationX(moveX);
            });
            nextBtn.setEnabled(true);
            nextBtn.setAlpha(1.0f);
        }
        // Otherwise check if we have a phone saved
        else if (!RegDataHolder.phone.isEmpty()) {
            input.setText(RegDataHolder.phone);
            input.setInputType(InputType.TYPE_CLASS_PHONE);
            input.setHint(R.string.phone_number);
            label.setText(R.string.phone_number);

            selector.setTranslationX(0);
            nextBtn.setEnabled(true);
            nextBtn.setAlpha(1.0f);
        }

        // Set selection to end of text
        input.setSelection(input.getText().length());
    }

    private void registerReq() {
        // Set up UI for loading state
        midRequest = true;
        Helpers.startDotsAnimation(this, nextBtn, input, null);

        // Create the request object
        RegisterRequest request = new RegisterRequest();

        // Send to Pi
        RetrofitClient.getApiService().register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RegDataHolder.id = response.body().getId();
                    String receivedToken = response.body().getToken(); // Grab the 64-char string

                    // Save it to the phone disk
                    SessionManager sessionManager = new SessionManager(getApplicationContext());
                    sessionManager.saveLoginSession(receivedToken);
                    handleRegistrationError(1);
                } else {
                    // Handle Error Codes
                    serverWarningTxt.setVisibility(View.GONE);
                    Helpers.resetUI(RegisterActivity.this, nextBtn, input, null);
                    midRequest = false;
                    try {
                        // Retrofit won't automatically parse the body on a 400 error,
                        // so we manually convert the errorBody to our object
                        assert response.errorBody() != null;
                        RegisterResponse errorRes = new Gson().fromJson(response.errorBody().charStream(), RegisterResponse.class);
                        handleRegistrationError(errorRes.getErrorCode());
                    } catch (Exception e) {
                        handleRegistrationError(999); // Generic error
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                serverWarningTxt.setVisibility(View.VISIBLE);
                Helpers.resetUI(RegisterActivity.this, nextBtn, input, null);
            }
        });
    }

    private void setupSelectorWidth() {
        blackBar.post(() -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) selector.getLayoutParams();
            int totalWidth = blackBar.getWidth();
            int totalMargins = (params.leftMargin + params.rightMargin) * 2;
            params.width = (totalWidth - totalMargins) / 2;
            selector.setLayoutParams(params);
        });
    }

    private void setupListeners() {
        btnPhone.setOnClickListener(v -> switchToPhone());
        btnEmail.setOnClickListener(v -> switchToEmail());

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = !s.toString().trim().isEmpty();
                nextBtn.setEnabled(hasText);
                nextBtn.animate().alpha(hasText ? 1.0f : 0.5f).setDuration(150).start();

                // Hide warning when user starts fixing the mistake
                warningTxt.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void switchToPhone() {
        if (midRequest) { return; }
        RegDataHolder.email = input.getText().toString().trim();
        RegDataHolder.focused = 1;
        input.setText(RegDataHolder.phone);
        input.setSelection(input.getText().length());
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setHint(R.string.phone_number);
        label.setText(R.string.phone_number);

        selector.animate().translationX(0).setDuration(200).start();
    }

    private void switchToEmail() {
        if (midRequest) { return; }
        RegDataHolder.phone = input.getText().toString().trim();
        RegDataHolder.focused = 2;
        input.setText(RegDataHolder.email);
        input.setSelection(input.getText().length());
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint(R.string.email);
        label.setText(R.string.email);

        float moveX = blackBar.getWidth() / 2f;
        selector.animate().translationX(moveX).setDuration(200).start();
    }

    private void handleRegistrationError(int code) {
        if (code == 1) {
            RegDataHolder.registered = true;
            finish();
        } else if (code == 102) {
            warningTxt.setVisibility(View.VISIBLE);
            warningTxt.setText(String.format("This %s is already taken!", (RegDataHolder.focused == 1) ? "phone number" : "email"));
            nextBtn.setText(R.string.create_account);
            input.requestFocus();
            // Show keyboard automatically
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void openRegDisplayActivity(View v) {
        String identifier = input.getText().toString().trim();

        if (RegDataHolder.focused == 2) {
            if (!Helpers.isValidEmail(identifier)) {
                warningTxt.setVisibility(View.VISIBLE);
                warningTxt.setText(R.string.please_enter_a_valid_email_address);
                return;
            }
        }

        saveIdentifier();
        if (RegDataHolder.errorCode == 102) {
            registerReq();
        } else {
            Intent intent = new Intent(this, RegDisplayActivity.class);
            startActivity(intent);
        }
    }

    public void finish(View v) {
        saveIdentifier();
        finish();
    }

    public void saveIdentifier() {
        String identifier = input.getText().toString().trim();
        if (RegDataHolder.focused == 1) {
            RegDataHolder.phone = identifier;
        } else {
            RegDataHolder.email = identifier;
        }
    }
}