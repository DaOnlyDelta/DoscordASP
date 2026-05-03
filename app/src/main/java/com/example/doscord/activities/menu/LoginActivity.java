package com.example.doscord.activities.menu;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doscord.api.LoginRequest;
import com.example.doscord.api.LoginResponse;
import com.example.doscord.R;
import com.example.doscord.api.RetrofitClient;
import com.example.doscord.utils.Helpers;
import com.example.doscord.utils.LogDataHolder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText identifierInput, passInput;
    private TextView identifierWarningTxt, passWarningTxt, serverWarningTxt;
    private ImageButton backBtn, eyeBtn;
    private boolean isPasswordVisible = false;
    private Button loginBtn;

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

        initViews();
    }

    private void initViews() {
        identifierInput = findViewById(R.id.logIdentifierInput);
        passInput = findViewById(R.id.logPassInput);
        backBtn = findViewById(R.id.logBackBtn);
        eyeBtn = findViewById(R.id.logPassEyeBtn);
        loginBtn = findViewById(R.id.logNextBtn);
        identifierWarningTxt = findViewById(R.id.logIdentifierWarning);
        passWarningTxt = findViewById(R.id.logPassWarning);
        serverWarningTxt = findViewById(R.id.logServerWarning);

        identifierInput.setText(LogDataHolder.identifier);
        passInput.setText(LogDataHolder.password);
        updateWarningVisibility();
    }

    private void updateWarningVisibility() {
        identifierWarningTxt.setVisibility((LogDataHolder.error) ? View.VISIBLE : View.GONE);
        passWarningTxt.setVisibility((LogDataHolder.error) ? View.VISIBLE : View.GONE);
    }

    public void forgotPassword(View v) {
        // Forgot password code placeholder
    }

    public void loginReq(View v) {
        String identifier = identifierInput.getText().toString().trim();
        String password = passInput.getText().toString().trim();

        // Check if empty
        if (identifier.isEmpty()) {
            identifierInput.requestFocus();
            return;
        } else if (password.trim().isEmpty()) {
            passInput.requestFocus();
            return;
        }

        // Lock UI & Close keyboard
        Helpers.startDotsAnimation(this, loginBtn);
        backBtn.setEnabled(false);
        identifierInput.setEnabled(false);
        passInput.setEnabled(false);

        // Execute (Using the class-level apiService initialized in onCreate)
        LoginRequest loginRequest = new LoginRequest(identifier, password);

        RetrofitClient.getApiService().login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse.User user = response.body().getUser();
                    LogDataHolder.setResponseData(user.getId(), user.getUsername(), user.getDisplayName(), user.getPfp());
                    loginBtn.setText(user.getPfp());
                    finish();
                } else {
                    serverWarningTxt.setVisibility(View.GONE);
                    LogDataHolder.error = true;
                    updateWarningVisibility();
                    Helpers.resetUI(LoginActivity.this, loginBtn, backBtn, identifierInput, passInput);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                serverWarningTxt.setVisibility(View.VISIBLE);
                Helpers.resetUI(LoginActivity.this, loginBtn, backBtn, identifierInput, passInput);
            }
        });
    }

    public void logSwitchEye(View v) {
        isPasswordVisible = Helpers.switchEye(isPasswordVisible, passInput, eyeBtn);
    }

    public void finish(View v) {
        LogDataHolder.identifier = identifierInput.getText().toString().trim();
        LogDataHolder.password = passInput.getText().toString();
        finish();
    }
}