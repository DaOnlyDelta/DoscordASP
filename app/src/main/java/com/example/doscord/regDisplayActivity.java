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

public class regDisplayActivity extends AppCompatActivity {

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
        Button nextButton = findViewById(R.id.regDNext);
        EditText input = findViewById(R.id.regDDisplayName);
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

    public void finish(View v) {
        finish();
    }

    public void skip(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        // intent.setDisplayName();
        startActivity(intent);
    }

    public void openRegCreate(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        String displayName = ((EditText) findViewById(R.id.regDDisplayName)).getText().toString();
        // intent.setDisplayName();
        startActivity(intent);
    }
}