package com.example.doscord.activities.chatroom;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doscord.R;
import com.example.doscord.activities.menu.MainActivity;
import com.example.doscord.activities.menu.RegPfpActivity;
import com.example.doscord.api.LogoutRequest;
import com.example.doscord.api.RetrofitClient;
import com.example.doscord.api.TokenLoginResponse;
import com.example.doscord.api.UpdateRequest;
import com.example.doscord.api.UpdateResponse;
import com.example.doscord.adapters.ChatsAdapter;
import com.example.doscord.models.Channel;
import com.example.doscord.utils.GlobalData;
import com.example.doscord.utils.LogDataHolder;
import com.example.doscord.utils.PfpUtils;
import com.example.doscord.utils.RegDataHolder;
import com.example.doscord.models.User;
import com.example.doscord.adapters.RequestsAdapter;
import com.example.doscord.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrMainActivity extends AppCompatActivity {
    private ImageView homeIcon, notifIcon, pfpImg, profileIcon;
    private TextView homeTxt, notifTxt, pfpTxt, profileDisplayName, profileUsername;
    private int selected = 0;
    private RecyclerView homeChatsView;
    private ConstraintLayout homeLayout, notifLayout, emptyNotifLayout, profileLayout, overlayLayout;
    private int attempt = 0;

    // Updates
    private long lastSyncTime = 0;
    private final Handler updateHandler = new Handler();
    private Runnable updateRunnable;
    private final int PING_INTERVAL = 5000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cr_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, 0, 0);
            return insets;
        });

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchData();
        startUpdateLoop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }

    private void initViews() {
        homeIcon = findViewById(R.id.crMainHomeIcon);
        homeTxt = findViewById(R.id.crMainHomeText);
        notifIcon = findViewById(R.id.crMainNotifIcon);
        notifTxt = findViewById(R.id.crMainNotifText);
        pfpImg = findViewById(R.id.crMainPfpIcon);
        pfpTxt = findViewById(R.id.crMainPfpText);

        homeLayout = findViewById(R.id.crMainHomeContainer);
        homeChatsView = findViewById(R.id.crMainRecyclerView);
        homeChatsView.setLayoutManager(new LinearLayoutManager(this));

        notifLayout = findViewById(R.id.crMainNotifContainer);
        emptyNotifLayout = findViewById(R.id.crMainEmptyNotifLayout);

        profileLayout = findViewById(R.id.crMainProfileContainer);
        profileIcon = findViewById(R.id.crProfileIcon);
        profileUsername = findViewById(R.id.crProfileUsernameTxt);
        profileDisplayName = findViewById(R.id.crProfileDisplayNameTxt);

        overlayLayout = findViewById(R.id.crMainOverlay);
    }

    private void fetchData() {
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        RetrofitClient.getApiService().tokenLogin(token).enqueue(new Callback<TokenLoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<TokenLoginResponse> call, @NonNull Response<TokenLoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // FILL THE STATIC REPOSITORY FIRST
                    GlobalData.updateData(response.body());

                    // SAVE THE TIMESTAMP FROM BACKEND
                    lastSyncTime = response.body().getSyncTimestamp();

                    displayData();
                } else {
                    // ADD DEBUGGING LOGS HERE
                    Log.e("DEBUG_NETWORK", "Token Login Failed! Status: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            Log.e("DEBUG_NETWORK", "Error Body: " + response.errorBody().string());
                        }
                    } catch (Exception ignored) {}

                    if (attempt < 2) {
                        attempt++;
                        fetchData();
                    } else {
                        new SessionManager(CrMainActivity.this).logout();
                        Intent intent = new Intent(CrMainActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<TokenLoginResponse> call, @NonNull Throwable t) {
                fetchData();
            }
        });
    }

    private void displayData() {
        User me = GlobalData.getMyProfile(); // Updated to getMyProfile()
        if (me != null) {
            PfpUtils.loadMyPfp(this, me.getPfp(), pfpImg, profileIcon);
            profileUsername.setText(me.getUsername());
            profileDisplayName.setText((me.getDisplayName() != null) ? me.getDisplayName() : me.getUsername());
        } else {
            Log.e("CrMainActivity", "GlobalData.getMyProfile() returned null!");
        }
        setupRecyclerView();

        overlayLayout.animate()
                .alpha(0f)
                .setDuration(500)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        overlayLayout.setVisibility(View.GONE);
                    }
                });
    }

    private void setupRecyclerView() {
        LogDataHolder.clear();

        // 1. Load active communication rows directly via Channel channels payload
        List<Channel> channels = GlobalData.getChannelList();

        RecyclerView recyclerView = findViewById(R.id.crMainRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ChatsAdapter adapter = new ChatsAdapter(channels, this);
        recyclerView.setAdapter(adapter);

        // 2. Load dedicated complete pending User arrays straight to notifications matching fragment layout
        List<User> requestsOnly = GlobalData.getPendingRequests();
        updateRequests(requestsOnly);
    }

    private void updateRequests(List<User> requestsOnly) {
        RecyclerView recyclerView = findViewById(R.id.crNotifRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RequestsAdapter requestsAdapter = new RequestsAdapter(requestsOnly, this, new RequestsAdapter.OnRequestHandledListener() {
            @Override
            public void onRequestProcessed() {
                fetchData();
            }
        });

        if (requestsAdapter.getItemCount() > 0) {
            emptyNotifLayout.setVisibility(View.GONE);
        } else {
            emptyNotifLayout.setVisibility(View.VISIBLE);
        }
        recyclerView.setAdapter(requestsAdapter);
    }

    public void openAddFriends(View v) {
        Intent intent = new Intent(this, AddFriendsActivity.class);
        startActivity(intent);
    }

    public void homeClicked(View v) {
        updateNav(0);
    }

    public void notifClicked(View v) {
        updateNav(1);
    }

    public void pfpClicked(View v) {
        updateNav(2);
    }

    private void updateNav(int index) {
        if (selected == index) return;
        selected = index;
        clearSelected();
    }

    private void clearSelected() {
        int selectedColor = ContextCompat.getColor(this, R.color.selected);
        int unselectedColor = ContextCompat.getColor(this, R.color.unselected);

        homeIcon.setColorFilter(unselectedColor, PorterDuff.Mode.SRC_IN);
        notifIcon.setColorFilter(unselectedColor, PorterDuff.Mode.SRC_IN);
        pfpImg.setAlpha(0.5f);

        homeTxt.setTextColor(unselectedColor);
        notifTxt.setTextColor(unselectedColor);
        pfpTxt.setTextColor(unselectedColor);

        homeLayout.setVisibility(View.GONE);
        notifLayout.setVisibility(View.GONE);
        profileLayout.setVisibility(View.GONE);

        switch (selected) {
            case 0:
                homeLayout.setVisibility(View.VISIBLE);
                homeIcon.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN);
                homeTxt.setTextColor(selectedColor);
                break;
            case 1:
                notifLayout.setVisibility(View.VISIBLE);
                notifIcon.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN);
                notifTxt.setTextColor(selectedColor);
                break;
            case 2:
                profileLayout.setVisibility(View.VISIBLE);
                pfpImg.setAlpha(1.0f);
                pfpTxt.setTextColor(selectedColor);
                break;
        }
    }

    private void startUpdateLoop() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                String token = new SessionManager(CrMainActivity.this).getToken();
                UpdateRequest updateReq = new UpdateRequest(token, lastSyncTime);

                RetrofitClient.getApiService().checkUpdates(updateReq).enqueue(new Callback<UpdateResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UpdateResponse> call, @NonNull Response<UpdateResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().isUpdateRequired()) {
                                fetchData();
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

    public void logout(View v) {
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        RetrofitClient.getApiService().logout(new LogoutRequest(token)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {}
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
        });
        sessionManager.logout();
        GlobalData.clear();
        RegDataHolder.clear();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void changePfp(View v) {
        Intent intent = new Intent(this, RegPfpActivity.class);
        startActivity(intent);
    }

    public void newChat(View v) {
        Intent intent = new Intent(this, CrNewChatActivity.class);
        startActivity(intent);
    }
}