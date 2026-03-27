package com.example.doscord;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        String emailOrUsername = ((EditText) findViewById(R.id.LogMailOrPhone)).getText().toString();
        String password = ((EditText) findViewById(R.id.logPassword)).getText().toString();

        // Handle the req to the db here
    }

    public void showPass(View v) {
        if (isPasswordVisible) {
            // Hide password
            Log.d("Password Status", "Shown!");
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            eyeButton.setImageResource(R.drawable.eye_open);
        } else {
            // Show password
            Log.d("Password Status", "Hidden!");
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            eyeButton.setImageResource(R.drawable.eye_closed);
        }
        passwordEditText.setSelection(passwordEditText.getText().length()); // move the cursor to the very end of the text
        isPasswordVisible = !isPasswordVisible; // flip state
    }
}