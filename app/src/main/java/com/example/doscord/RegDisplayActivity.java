package com.example.doscord;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegDisplayActivity extends AppCompatActivity {

    private EditText input;
    private Button skipButton, nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reg_display);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Button toggling
        initViews();
        inputToggling();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // AUTO-BACK: If we are returning because of an email/phone error (102) or successful (1),
        if (RegDataHolder.errorCode == 102 || RegDataHolder.errorCode == 1) {
            finish();
        }
    }

    public void initViews() {
        nextButton = findViewById(R.id.regDNext);
        skipButton = findViewById(R.id.regDSkip);
        input = findViewById(R.id.regDDisplayName);

        if (!RegDataHolder.displayName.isEmpty()) {
            input.setText(RegDataHolder.displayName);
            nextButton.setEnabled(true);
            nextButton.setAlpha(1.0f);
            skipButton.setEnabled(false);
            skipButton.setAlpha(0.5f);
        }
    }

    public void inputToggling() {
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = !s.toString().trim().isEmpty();
                nextButton.setEnabled(hasText);
                nextButton.animate().alpha(hasText ? 1.0f : 0.5f).setDuration(150).start();
                skipButton.setEnabled(!hasText);
                skipButton.animate().alpha(!hasText ? 1.0f : 0.5f).setDuration(150).start();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public void finish(View v) {
        RegDataHolder.displayName = input.getText().toString().trim();
        finish();
    }

    public void skip(View v) {
        Intent intent = new Intent(this, RegFinalActivity.class);
        startActivity(intent);
    }

    public void openFinalReg(View v) {
        RegDataHolder.displayName = input.getText().toString().trim();

        Intent intent = new Intent(this, RegFinalActivity.class);
        startActivity(intent);
    }
}