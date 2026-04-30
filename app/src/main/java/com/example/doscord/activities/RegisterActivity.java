package com.example.doscord.activities;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doscord.R;
import com.example.doscord.utils.Helpers;
import com.example.doscord.utils.RegDataHolder;

public class RegisterActivity extends AppCompatActivity {

    private View selector;
    private FrameLayout blackBar;
    private TextView label, warningTxt;
    private EditText input;
    private Button btnPhone, btnEmail, nextButton;

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
        handlePotentialErrorReturn();
    }

    private void initViews() {
        selector = findViewById(R.id.regSelector);
        blackBar = findViewById(R.id.regBar);
        label = findViewById(R.id.regLabel);
        input = findViewById(R.id.regIdentifierInput);
        btnPhone = findViewById(R.id.regPhoneBtn);
        btnEmail = findViewById(R.id.regEmailBtn);
        nextButton = findViewById(R.id.regNextBtn);
        warningTxt = findViewById(R.id.regWarning);
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
            nextButton.setEnabled(true);
            nextButton.setAlpha(1.0f);
        }
        // Otherwise check if we have a phone saved
        else if (!RegDataHolder.phone.isEmpty()) {
            input.setText(RegDataHolder.phone);
            input.setInputType(InputType.TYPE_CLASS_PHONE);
            input.setHint(R.string.phone_number);
            label.setText(R.string.phone_number);

            selector.setTranslationX(0);
            nextButton.setEnabled(true);
            nextButton.setAlpha(1.0f);
        }

        // Set selection to end of text
        input.setSelection(input.getText().length());
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
                nextButton.setEnabled(hasText);
                nextButton.animate().alpha(hasText ? 1.0f : 0.5f).setDuration(150).start();

                // Hide warning when user starts fixing the mistake
                warningTxt.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void switchToPhone() {
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

    private void handlePotentialErrorReturn() {
        if (RegDataHolder.errorCode == 1) {
            RegDataHolder.clear();
            finish();
        } else if (RegDataHolder.errorCode == 102) {
            warningTxt.setVisibility(View.VISIBLE);
            warningTxt.setText(String.format("This %s is already taken!", (RegDataHolder.focused == 1) ? "phone number" : "email"));
            input.requestFocus();
            // Show keyboard automatically
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);

            RegDataHolder.errorCode = 0;
        }
    }

    public void toDisplayReg(View v) {
        String identifier = input.getText().toString().trim();

        if (RegDataHolder.focused == 2) {
            if (!Helpers.isValidEmail(identifier)) {
                warningTxt.setVisibility(View.VISIBLE);
                warningTxt.setText(R.string.please_enter_a_valid_email_address);
                return;
            }
        }

        saveIdentifier();
        Intent intent = new Intent(this, RegDisplayActivity.class);
        startActivity(intent);
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