package com.example.doscord.activities.chatroom;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doscord.R;
import com.example.doscord.api.FriendRequestRequest;
import com.example.doscord.api.FriendRequestResponse;
import com.example.doscord.api.RetrofitClient;
import com.example.doscord.utils.Helpers;
import com.example.doscord.utils.LogDataHolder;
import com.example.doscord.utils.RegDataHolder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddByUserActivity extends AppCompatActivity {

    private EditText usernameInput;
    private TextView usernameLabel;
    private ImageButton backBtn;
    private Button sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_by_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
    }

    private void initViews() {
        usernameInput = findViewById(R.id.crAddByUserInput);
        usernameLabel = findViewById(R.id.crAddByUserLabelUsername);
        backBtn = findViewById(R.id.crAddByUserBackBtn);
        sendBtn = findViewById(R.id.crAddByUserSend);

        usernameLabel.setText((RegDataHolder.username.isEmpty()) ? LogDataHolder.getUsername() : RegDataHolder.username);
    }

    public void sendFriendReq(View v) {
        String friendUsername = usernameInput.getText().toString().trim();

        if (friendUsername.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = LogDataHolder.getId();
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }


        Helpers.startDotsAnimation(this, sendBtn, backBtn);
        FriendRequestRequest request = new FriendRequestRequest(userId, friendUsername);
        RetrofitClient.getApiService().sendFriendRequest(request).enqueue(new Callback<FriendRequestResponse>() {
            @Override
            public void onResponse(@NonNull Call<FriendRequestResponse> call, @NonNull Response<FriendRequestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Integer errorCode = response.body().getErrorCode();
                    if (errorCode == null) {
                        Toast.makeText(AddByUserActivity.this, "Friend request sent!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        handleError(errorCode);
                        Helpers.resetUI(AddByUserActivity.this, sendBtn, backBtn, usernameInput, null);
                    }
                } else {
                    Toast.makeText(AddByUserActivity.this, "Server error. Try again later.", Toast.LENGTH_SHORT).show();
                    Helpers.resetUI(AddByUserActivity.this, sendBtn, backBtn, usernameInput, null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<FriendRequestResponse> call, @NonNull Throwable t) {
                Toast.makeText(AddByUserActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Helpers.resetUI(AddByUserActivity.this, sendBtn, backBtn, usernameInput, null);
            }
        });
    }

    private void handleError(int errorCode) {
        String message;
        switch (errorCode) {
            case 1:
                message = "User not found";
                break;
            case 2:
                message = "You are already friends with this user";
                break;
            case 3:
                message = "Friend request already pending";
                break;
            case 4:
                message = "You cannot add yourself";
                break;
            case 99:
                message = "Something went wrong on the server";
                break;
            default:
                message = "Unknown error occurred";
                break;
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void finish(View v) {
        finish();
    }
}