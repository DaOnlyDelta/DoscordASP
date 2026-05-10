package com.example.doscord.activities.chatroom;

import android.os.Bundle;
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
import com.example.doscord.api.MessagesRequest;
import com.example.doscord.api.MessagesResponse;
import com.example.doscord.api.RetrofitClient;
import com.example.doscord.utils.Message;
import com.example.doscord.utils.MessagesAdapter;
import com.example.doscord.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MessagesAdapter adapter;
    private final List<Message> messagesList = new ArrayList<>();
    private int channelId;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cr_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        loadMessages();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.crChatChat);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MessagesAdapter(messagesList, this);
        recyclerView.setAdapter(adapter);

        sessionManager = new SessionManager(this);
        channelId = getIntent().getIntExtra("channel_id", -1);
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
                    // Scroll to bottom
                    if (!messagesList.isEmpty()) {
                        recyclerView.scrollToPosition(messagesList.size() - 1);
                    }
                } else {
                    Toast.makeText(CrChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessagesResponse> call, @NonNull Throwable t) {
                Toast.makeText(CrChatActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
