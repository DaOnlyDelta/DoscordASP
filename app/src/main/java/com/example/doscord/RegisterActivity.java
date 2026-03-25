package com.example.doscord;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

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

        View selector = findViewById(R.id.selector);
        FrameLayout blackBar = findViewById(R.id.blackBar);

        blackBar.post(() -> {
            int width = blackBar.getWidth();
            ViewGroup.LayoutParams params = selector.getLayoutParams();
            params.width = width / 2;
            selector.setLayoutParams(params);
        });

        Button btnPhone = findViewById(R.id.btnPhone);
        Button btnEmail = findViewById(R.id.btnEmail);

        btnPhone.setOnClickListener(v -> {
            selector.animate()
                    .translationX(0)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        });

        btnEmail.setOnClickListener(v -> {
            float moveX = blackBar.getWidth() / 2f;

            selector.animate()
                    .translationX(moveX)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        });
    }

    public void backToHome(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}