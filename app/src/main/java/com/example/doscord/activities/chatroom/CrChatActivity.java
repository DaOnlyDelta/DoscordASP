package com.example.doscord.activities.chatroom;

import android.annotation.SuppressLint;
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
import com.example.doscord.api.NewMessagesRequest;
import com.example.doscord.api.OlderMessagesRequest;
import com.example.doscord.api.RetrofitClient;
import com.example.doscord.utils.GlobalData;
import com.example.doscord.utils.Message;
import com.example.doscord.utils.MessagesAdapter;
import com.example.doscord.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrChatActivity extends AppCompatActivity {

    public static int activeChannelId = -1;
    private RecyclerView recyclerView;
    private EditText messageInput;
    private TextView chatTitle;
    private View micBtn, sendBtn;
    private MessagesAdapter adapter;
    private final List<Message> messagesList = new ArrayList<>();
    private int channelId;
    private boolean canLoadMore = true; // Track if the server has more history
    private boolean isLoading = false;
    private boolean isLoadingNew = false;

    // Updates
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
            if (ime.bottom > 0 && adapter != null && adapter.getItemCount() > 0) {
                recyclerView.postDelayed(() -> recyclerView.scrollToPosition(adapter.getItemCount() - 1), 100);
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
        activeChannelId = channelId;
        NotificationHelper.clearActiveStyle(channelId);
        startChatUpdateLoop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        activeChannelId = -1;
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

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();

                // If scrolling up and we see the loader, trigger "loadOlder"
                if (canLoadMore && !isLoading && dy < 0 && lm != null && lm.findFirstVisibleItemPosition() == 0) {
                    loadOlder();
                }
            }
        });

        channelId = getIntent().getIntExtra("channel_id", -1);
        String chatName = getIntent().getStringExtra("chat_name");
        if (chatName != null) {
            chatTitle.setText(chatName);
        }

        micBtn.setOnClickListener(v -> Toast.makeText(this, "Voice messages coming soon!", Toast.LENGTH_SHORT).show());
    }

    /**
     * Call this once in onCreate. Gets the most recent 25 messages.
     */
    private void loadMessages() {
        if (channelId == -1 || isLoading) return;
        isLoading = true;

        MessagesRequest req = new MessagesRequest(channelId);
        RetrofitClient.getApiService().getMessages(req).enqueue(new Callback<MessagesResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<MessagesResponse> call, @NonNull Response<MessagesResponse> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    MessagesResponse data = response.body();
                    canLoadMore = data.isHasMore();

                    messagesList.clear();
                    messagesList.addAll(data.getMessages());

                    adapter.setShowLoader(canLoadMore);
                    adapter.updateDisplayList();
                    adapter.notifyDataSetChanged();

                    updateLastSeenTimestamp();

                    recyclerView.post(() -> recyclerView.scrollToPosition(adapter.getItemCount() - 1));
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessagesResponse> call, @NonNull Throwable t) {
                isLoading = false;
            }
        });
    }

    /**
     * Triggers when user hits the top. Fetches messages BEFORE the oldest current ID.
     */
    private void loadOlder() {
        if (messagesList.isEmpty() || !canLoadMore || isLoading) return;
        isLoading = true;
        adapter.setLoaderPlaying(true);

        // Save anchor for scroll restoration
        LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
        int anchorId = -1;
        int anchorTop = 0;
        if (lm != null) {
            int firstVisible = lm.findFirstVisibleItemPosition();
            if (firstVisible != RecyclerView.NO_POSITION) {
                View v = lm.findViewByPosition(firstVisible);
                if (v != null) {
                    anchorTop = v.getTop();
                    List<Object> currentDisplayList = adapter.getDisplayList();
                    if (firstVisible < currentDisplayList.size()) {
                        Object item = currentDisplayList.get(firstVisible);
                        if (item instanceof Message) {
                            anchorId = ((Message) item).getId();
                        } else {
                            // Find first message to pin to
                            for (int i = firstVisible + 1; i < currentDisplayList.size(); i++) {
                                if (currentDisplayList.get(i) instanceof Message) {
                                    anchorId = ((Message) currentDisplayList.get(i)).getId();
                                    View nextV = lm.findViewByPosition(i);
                                    if (nextV != null) anchorTop = nextV.getTop();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cursor: The ID of our oldest message
        int oldestId = messagesList.get(0).getId();

        OlderMessagesRequest req = new OlderMessagesRequest(channelId, oldestId);
        final int finalAnchorId = anchorId;
        final int finalAnchorTop = anchorTop;

        RetrofitClient.getApiService().getOlderMessages(req).enqueue(new Callback<MessagesResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<MessagesResponse> call, @NonNull Response<MessagesResponse> response) {
                isLoading = false;
                adapter.setLoaderPlaying(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Message> olderMessages = response.body().getMessages();
                    if (olderMessages.isEmpty()) {
                        canLoadMore = false;
                        adapter.setShowLoader(false);
                        return;
                    }

                    // Prepend data
                    canLoadMore = response.body().isHasMore();
                    messagesList.addAll(0, olderMessages);

                    adapter.setShowLoader(canLoadMore);
                    adapter.updateDisplayList();
                    adapter.notifyDataSetChanged();

                    // Restore scroll relative to anchor
                    if (lm != null && finalAnchorId != -1) {
                        List<Object> displayList = adapter.getDisplayList();
                        for (int i = 0; i < displayList.size(); i++) {
                            Object obj = displayList.get(i);
                            if (obj instanceof Message && ((Message) obj).getId() == finalAnchorId) {
                                lm.scrollToPositionWithOffset(i, finalAnchorTop);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessagesResponse> call, @NonNull Throwable t) {
                isLoading = false;
                adapter.setLoaderPlaying(false);
            }
        });
    }

    /**
     * The Update Loop: Only fetches messages AFTER the newest current ID.
     */
    private void startChatUpdateLoop() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                fetchNewMessages(false);
                updateHandler.postDelayed(this, PING_INTERVAL);
            }
        };
        updateHandler.postDelayed(updateRunnable, PING_INTERVAL);
    }

    private void fetchNewMessages(boolean forceScroll) {
        if (messagesList.isEmpty() || isLoadingNew) return;
        isLoadingNew = true;

        int newestId = messagesList.get(messagesList.size() - 1).getId();
        NewMessagesRequest req = new NewMessagesRequest(channelId, newestId);

        RetrofitClient.getApiService().getNewMessages(req).enqueue(new Callback<MessagesResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<MessagesResponse> call, @NonNull Response<MessagesResponse> response) {
                isLoadingNew = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<Message> newOnes = response.body().getMessages();
                    if (!newOnes.isEmpty()) {
                        LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                        boolean isAtBottom = lm != null && lm.findLastVisibleItemPosition() >= adapter.getItemCount() - 2;

                        messagesList.addAll(newOnes);
                        adapter.updateDisplayList();
                        adapter.notifyDataSetChanged();

                        updateLastSeenTimestamp();

                        if (forceScroll || isAtBottom) {
                            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessagesResponse> call, @NonNull Throwable t) {
                isLoadingNew = false;
            }
        });
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
                    fetchNewMessages(true);
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

    private void updateLastSeenTimestamp() {
        if (messagesList.isEmpty()) return;
        String latestTime = messagesList.get(messagesList.size() - 1).getSentAt();
        getSharedPreferences("notif_prefs", MODE_PRIVATE)
                .edit()
                .putString("last_msg_time_" + channelId, latestTime)
                .apply();
    }

    public void finish(View v) {
        finish();
    }
}
