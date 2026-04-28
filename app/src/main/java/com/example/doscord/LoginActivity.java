package com.example.doscord;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    public static final String BASE_URL = "https://doscord-api.duckdns.org/";

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
        String identifier = ((EditText) findViewById(R.id.LogMailOrPhone)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.logPassword)).getText().toString().trim();

        // 1. Initialize Retrofit
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client) // Force the timeout
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // 2. Prepare the Request
        LoginRequest loginRequest = new LoginRequest(identifier, password);

        // 3. Execute the Call
        Log.d("LOGIN_DEBUG", "Sending: " + identifier + " with pass: " + password);
        apiService.login(loginRequest).enqueue(new retrofit2.Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<LoginResponse> call, @NonNull retrofit2.Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // SUCCESS!
                    String displayName = response.body().user.display_name;
                    android.widget.Toast.makeText(LoginActivity.this, "Welcome " + displayName, android.widget.Toast.LENGTH_SHORT).show();

                    // TODO: Start your HomeActivity here
                } else {
                    android.widget.Toast.makeText(LoginActivity.this, "Login Failed: Check credentials", android.widget.Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<LoginResponse> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Message: " + t.getMessage()); // Add this!
                android.widget.Toast.makeText(LoginActivity.this, "Server Error", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
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