package com.example.doscord.activities.chatroom;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doscord.R;
import com.example.doscord.api.ApiService;
import com.example.doscord.api.MessageRequest;
import com.example.doscord.api.MessagesRequest;
import com.example.doscord.api.MessagesResponse;
import com.example.doscord.api.RetrofitClient;
import com.example.doscord.api.UpdateRequest;
import com.example.doscord.api.UpdateResponse;
import com.example.doscord.utils.GlobalData;
import com.example.doscord.utils.Message;
import com.example.doscord.utils.MessagesAdapter;
import com.example.doscord.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText messageInput;
    private TextView chatTitle;
    private View micBtn, sendBtn;
    private MessagesAdapter adapter;
    private final List<Message> messagesList = new ArrayList<>();
    private int channelId;
    private SessionManager sessionManager;

    // Updates
    private long lastSyncTime = 0;
    private final Handler updateHandler = new Handler();
    private Runnable updateRunnable;
    private final int PING_INTERVAL = 3000; // Chats feel better with a 3s check

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cr_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
            
            // Scroll to bottom if keyboard opened and we have messages
            if (ime.bottom > 0 && !messagesList.isEmpty()) {
                recyclerView.postDelayed(() -> recyclerView.scrollToPosition(messagesList.size() - 1), 100);
            }

            return insets;
        });

        initViews();
        buttonSwitching();
        loadMessages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startChatUpdateLoop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.crChatChat);
        messageInput = findViewById(R.id.crChatTextInput);
        chatTitle = findViewById(R.id.crChatTitle);
        micBtn = findViewById(R.id.crChatVMContainer);
        sendBtn = findViewById(R.id.crChatSendContainer);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MessagesAdapter(messagesList, this);
        recyclerView.setAdapter(adapter);

        sessionManager = new SessionManager(this);
        channelId = getIntent().getIntExtra("channel_id", -1);
        String chatName = getIntent().getStringExtra("chat_name");
        if (chatName != null) {
            chatTitle.setText(chatName);
        }

        micBtn.setOnClickListener(v -> Toast.makeText(this, "Voice messages coming soon!", Toast.LENGTH_SHORT).show());
    }

    private void loadMessages() {
        if (channelId == -1) return;

        ApiService apiService = RetrofitClient.getApiService();
        MessagesRequest request = new MessagesRequest(sessionManager.getToken(), channelId, 20);

        apiService.getMessages(request).enqueue(new Callback<MessagesResponse>() {
            @Override
            public void onResponse(@NonNull Call<MessagesResponse> call, @NonNull Response<MessagesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messagesList.clear();
                    messagesList.addAll(response.body().getMessages());
                    adapter.notifyDataSetChanged();

                    // Update local sync time so we don't trigger an immediate refresh loop
                    lastSyncTime = System.currentTimeMillis();

                    if (!messagesList.isEmpty()) {
                        recyclerView.scrollToPosition(messagesList.size() - 1);
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<MessagesResponse> call, @NonNull Throwable t) {}
        });
    }

    private void startChatUpdateLoop() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                String token = sessionManager.getToken();
                UpdateRequest updateReq = new UpdateRequest(token, lastSyncTime);

                RetrofitClient.getApiService().checkUpdates(updateReq).enqueue(new Callback<UpdateResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UpdateResponse> call, @NonNull Response<UpdateResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().isUpdateRequired()) {
                                loadMessages(); // New message found!
                            }
                        }
                        updateHandler.postDelayed(updateRunnable, PING_INTERVAL);
                    }

                    @Override
                    public void onFailure(@NonNull Call<UpdateResponse> call, @NonNull Throwable t) {
                        updateHandler.postDelayed(updateRunnable, PING_INTERVAL);
                    }
                });
            }
        };
        updateHandler.postDelayed(updateRunnable, PING_INTERVAL);
    }

    public void buttonSwitching() {
        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    sendBtn.setVisibility(View.GONE);
                    micBtn.setVisibility(View.VISIBLE);
                } else {
                    sendBtn.setVisibility(View.VISIBLE);
                    micBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public void send(View v) {
        String text = messageInput.getText().toString().trim();
        if (!text.isEmpty()) {
            MessageRequest request = new MessageRequest(channelId, GlobalData.getActiveUserId(), -1, text);
            executeSendMessage(request, new ArrayList<>());
        }
    }

    public void executeSendMessage(MessageRequest request, List<MultipartBody.Part> files) {
        // Convert strings to RequestBody parts
        MultipartBody.Part cId = MultipartBody.Part.createFormData("channel_id", request.getChannelId());
        MultipartBody.Part sId = MultipartBody.Part.createFormData("sender_id", request.getSenderId());
        MultipartBody.Part msg = MultipartBody.Part.createFormData("message_text", request.getMessageText());

        ApiService apiService = RetrofitClient.getApiService();
        Call<Void> call = apiService.sendMessage(cId, sId, msg, files);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                // response.isSuccessful() checks for 2xx range (like your 201 Created)
                if (response.isSuccessful()) {
                    messageInput.setText("");
                    loadMessages();
                } else {
                    Toast.makeText(CrChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                // Handle Network Error
            }
        });
    }

    public void finish(View v) {
        finish();
    }
}
