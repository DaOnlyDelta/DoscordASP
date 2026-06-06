package com.example.doscord.activities.chatroom;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doscord.R;
import com.example.doscord.api.FriendRequestRequest;
import com.example.doscord.api.FriendRequestResponse;
import com.example.doscord.api.RetrofitClient;
import com.example.doscord.utils.GlobalData;
import com.example.doscord.utils.Helpers;
import com.example.doscord.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddByUserActivity extends AppCompatActivity {

    private EditText usernameInput;
    private TextView usernameLabel, warningTxt;
    private ImageButton backBtn;
    private Button sendBtn;
    private User me;

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
        backBtn = findViewById(R.id.backBtn);
        sendBtn = findViewById(R.id.crAddByUserBtn);
        warningTxt = findViewById(R.id.crAddByUserWarning);
        me = GlobalData.getMyProfile();

        if (me != null) {
            usernameLabel.setText(me.getUsername());
        }
    }

    public void sendFriendReq(View v) {
        String friendUsername = usernameInput.getText().toString().trim();

        if (friendUsername.isEmpty()) {
            usernameInput.requestFocus();
            return;
        }

        if (me.getUsername().contentEquals(usernameInput.getText())) {
            handleError(97);
            return;
        }

        int userId = GlobalData.getActiveUserId();

        Helpers.startDotsAnimation(this, sendBtn, usernameInput, null);
        FriendRequestRequest request = new FriendRequestRequest(userId, friendUsername);
        RetrofitClient.getApiService().sendFriendRequest(request).enqueue(new Callback<FriendRequestResponse>() {
            @Override
            public void onResponse(@NonNull Call<FriendRequestResponse> call, @NonNull Response<FriendRequestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Integer errorCode = response.body().getErrorCode();
                    if (errorCode == null) {
                        warningTxt.setVisibility(View.VISIBLE);
                        warningTxt.setText(R.string.friend_request_sent);
                        warningTxt.setTextColor(ContextCompat.getColor(AddByUserActivity.this, R.color.green));
                        usernameInput.setText("");
                    } else {
                        handleError(errorCode);
                    }
                } else {
                    handleError(98);
                }
                Helpers.resetUI(AddByUserActivity.this, sendBtn, usernameInput, null);
            }

            @Override
            public void onFailure(@NonNull Call<FriendRequestResponse> call, @NonNull Throwable t) {
                handleError(99);
                warningTxt.setText(R.string.generic_server_error);
                warningTxt.setTextColor(ContextCompat.getColor(AddByUserActivity.this, R.color.red));
                Helpers.resetUI(AddByUserActivity.this, sendBtn, usernameInput, null);
            }
        });
    }

    private void handleError(int errorCode) {
        String message;
        switch (errorCode) {
            case 1:
                message = "That user doesn't exist. Double check the spelling.";
                break;
            case 2:
                message = "You're already friends with that user!";
                break;
            case 3:
                message = "Friend request already pending.";
                break;
            case 97:
                message = "You cannot send a friend request to yourself.";
                break;
            case 98:
                message = "Server error. Please try again.";
                break;
            case 99:
                message = "Something went wrong on the server!";
                break;
            default:
                message = "Unknown error occurred";
                break;
        }

        warningTxt.setVisibility(View.VISIBLE);
        warningTxt.setText(message);
        warningTxt.setTextColor(ContextCompat.getColor(AddByUserActivity.this, R.color.red));
    }

    public void finish(View v) {
        finish();
    }
}