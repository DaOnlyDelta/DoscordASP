package com.example.doscord;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    public static final String BASE_URL = "https://doscord-api.duckdns.org/";

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

        View selector = findViewById(R.id.regSelector);
        FrameLayout blackBar = findViewById(R.id.regBar);

        blackBar.post(() -> {
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) selector.getLayoutParams();

            int totalWidth = blackBar.getWidth();

            int totalMargins = (params.leftMargin + params.rightMargin) * 2;

            int selectorWidth = (totalWidth - totalMargins) / 2;

            params.width = selectorWidth;
            selector.setLayoutParams(params);
        });

        Button btnPhone = findViewById(R.id.regPhone);
        Button btnEmail = findViewById(R.id.regEmail);

        btnPhone.setOnClickListener(v ->
                selector.animate()
                        .translationX(0)
                        .setDuration(400)
                        .setInterpolator(new DecelerateInterpolator())
                        .start());

        btnEmail.setOnClickListener(v ->
                selector.animate()
                        .translationX(selector.getWidth())
                        .setDuration(400)
                        .setInterpolator(new DecelerateInterpolator())
                        .start()
        );

        // References
        TextView label = findViewById(R.id.regLabel);
        EditText input = findViewById(R.id.legPhoneOrEmail);

        // Use arrays to allow mutation inside lambdas
        final String[] phoneText = {""};
        final String[] emailText = {""};

        // Phone button
        btnPhone.setOnClickListener(v -> {
            // Save email text
            emailText[0] = input.getText().toString();

            // Restore phone text
            input.setText(phoneText[0]);
            input.setSelection(input.getText().length());

            label.setText(R.string.phone_number);
            input.setInputType(InputType.TYPE_CLASS_PHONE);
            input.setHint(R.string.phone_number);

            selector.animate()
                    .translationX(0)
                    .setDuration(200)
                    .start();
        });

        // Email button
        btnEmail.setOnClickListener(v -> {
            // Save phone text
            phoneText[0] = input.getText().toString();

            // Restore email text
            input.setText(emailText[0]);
            input.setSelection(input.getText().length());

            label.setText(R.string.email);
            input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            input.setHint(R.string.email);

            float moveX = blackBar.getWidth() / 2f;

            selector.animate()
                    .translationX(moveX)
                    .setDuration(200)
                    .start();
        });

        // Button toggling
        Button nextButton = findViewById(R.id.regNext);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = !s.toString().trim().isEmpty();

                nextButton.setEnabled(hasText);

                if (hasText) {
                    nextButton.setAlpha(1.0f); // fully visible
                } else {
                    nextButton.setAlpha(0.5f); // grayed out
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public void toDisplayReg(View v) {
        Intent intent = new Intent(this, regDisplayActivity.class);
        startActivity(intent);
    }
    public void finish(View v) {
        finish();
    }
}