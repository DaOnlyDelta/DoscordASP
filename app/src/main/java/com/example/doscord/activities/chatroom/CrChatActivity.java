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
import com.example.doscord.api.TokenLoginResponse;
import com.example.doscord.models.Channel;
import com.example.doscord.utils.GlobalData;
import com.example.doscord.models.Message;
import com.example.doscord.adapters.MessagesAdapter;
import com.example.doscord.utils.SessionManager;

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
    private boolean canLoadMore = true;
    private boolean isLoading = false;
    private boolean isLoadingNew = false;
    private String token;
    private Channel currentChannel; // Store the channel object globally inside activity

    private final Handler updateHandler = new Handler();
    private Runnable updateRunnable;
    private final int PING_INTERVAL = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cr_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, ime.bottom));

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
        fetchData();
        fetchNewMessages(true);
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
        channelId = getIntent().getIntExtra("channel_id", -1);

        recyclerView = findViewById(R.id.crChatChat);
        messageInput = findViewById(R.id.crChatTextInput);
        chatTitle = findViewById(R.id.crChatTitle);
        micBtn = findViewById(R.id.crChatVMContainer);
        sendBtn = findViewById(R.id.crChatSendContainer);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MessagesAdapter(messagesList, this);
        adapter.setUpdateListener(this::fetchData);
        recyclerView.setAdapter(adapter);

        // Find the current channel context from our repository
        currentChannel = lookupChannel();
        if (currentChannel != null) {
            adapter.setChannelContext(currentChannel);
        }

        token = (new SessionManager(this)).getToken();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (canLoadMore && !isLoading && dy < 0 && lm != null && lm.findFirstVisibleItemPosition() == 0) {
                    loadOlder();
                }
            }
        });

        String chatName = getIntent().getStringExtra("chat_name");
        if (chatName != null) {
            chatTitle.setText(chatName);
        }

        micBtn.setOnClickListener(v -> {});
    }

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

    private void loadOlder() {
        if (messagesList.isEmpty() || !canLoadMore || isLoading) return;
        isLoading = true;
        adapter.setLoaderPlaying(true);

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

                    canLoadMore = response.body().isHasMore();
                    messagesList.addAll(0, olderMessages);

                    adapter.setShowLoader(canLoadMore);
                    adapter.updateDisplayList();
                    adapter.notifyDataSetChanged();

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
        if (channelId == -1 || isLoadingNew) return;

        int newestId = 0;
        if (!messagesList.isEmpty()) {
            newestId = messagesList.get(messagesList.size() - 1).getId();
        }

        isLoadingNew = true;
        NewMessagesRequest req = new NewMessagesRequest(channelId, newestId, token);

        RetrofitClient.getApiService().getNewMessages(req).enqueue(new Callback<MessagesResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<MessagesResponse> call, @NonNull Response<MessagesResponse> response) {
                isLoadingNew = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<Message> incoming = response.body().getMessages();
                    if (incoming != null && !incoming.isEmpty()) {
                        // Avoid duplicates by filtering only IDs greater than our current newest
                        int currentMaxId = 0;
                        if (!messagesList.isEmpty()) {
                            currentMaxId = messagesList.get(messagesList.size() - 1).getId();
                        }

                        List<Message> filtered = new ArrayList<>();
                        for (Message m : incoming) {
                            if (m.getId() > currentMaxId) {
                                filtered.add(m);
                            }
                        }

                        if (filtered.isEmpty()) return;

                        LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                        boolean isAtBottom = lm != null && lm.findLastVisibleItemPosition() >= adapter.getItemCount() - 2;

                        messagesList.addAll(filtered);
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
            // Updated constructor variables to match your Message backend expectations
            MessageRequest request = new MessageRequest(channelId, GlobalData.getActiveUserId(), text);
            executeSendMessage(request, new ArrayList<>());
        }
    }

    public void executeSendMessage(MessageRequest request, List<MultipartBody.Part> files) {
        MultipartBody.Part cId = MultipartBody.Part.createFormData("channel_id", String.valueOf(request.getChannelId()));
        MultipartBody.Part sId = MultipartBody.Part.createFormData("sender_id", String.valueOf(request.getSenderId()));
        MultipartBody.Part msg = MultipartBody.Part.createFormData("message_text", request.getMessageText());

        ApiService apiService = RetrofitClient.getApiService();
        Call<Void> call = apiService.sendMessage(cId, sId, msg, files);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    messageInput.setText("");
                    fetchNewMessages(true);
                } else {
                    Toast.makeText(CrChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(CrChatActivity.this, "Network error", Toast.LENGTH_SHORT).show();
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

    // Safely look up the exact channel object mapping from our shared repository
    private void fetchData() {
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        RetrofitClient.getApiService().tokenLogin(token).enqueue(new Callback<TokenLoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<TokenLoginResponse> call, @NonNull Response<TokenLoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GlobalData.updateData(response.body());
                    
                    // Update current channel context
                    currentChannel = lookupChannel();
                    if (currentChannel != null) {
                        adapter.setChannelContext(currentChannel);
                        if (currentChannel.isGroup()) {
                            chatTitle.setText(currentChannel.getGroupName());
                        } else {
                            chatTitle.setText(currentChannel.getDmDisplayNameOrNickname());
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<TokenLoginResponse> call, @NonNull Throwable t) {
                // Silently fail or retry
            }
        });
    }

    private Channel lookupChannel() {
        if (channelId == -1) return null;
        for (Channel c : GlobalData.getChannelList()) {
            if (c.getChannelId() == channelId) {
                return c;
            }
        }
        return null;
    }
}