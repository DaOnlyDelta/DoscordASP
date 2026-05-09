package com.example.doscord.activities.chatroom;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.example.doscord.R;
import com.example.doscord.activities.menu.MainActivity;
import com.example.doscord.api.LogoutRequest;
import com.example.doscord.api.RetrofitClient;
import com.example.doscord.api.TokenLoginResponse;
import com.example.doscord.utils.FriendsAdapter;
import com.example.doscord.utils.GlobalData;
import com.example.doscord.utils.LogDataHolder;
import com.example.doscord.utils.RegDataHolder;
import com.example.doscord.utils.RequestsAdapter;
import com.example.doscord.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrMainActivity extends AppCompatActivity {
    private ImageView homeIcon, notifIcon, pfpImg;
    private TextView homeTxt, notifTxt, pfpTxt;
    private int selected = 0;
    private RecyclerView homeChatsView;
    private ConstraintLayout homeLayout, notifLayout, emptyNotifLayout, profileLayout, overlayLayout;

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

    private void initViews() {
        // Bottom
        homeIcon = findViewById(R.id.crMainHomeIcon);
        homeTxt = findViewById(R.id.crMainHomeText);
        notifIcon = findViewById(R.id.crMainNotifIcon);
        notifTxt = findViewById(R.id.crMainNotifText);
        pfpImg = findViewById(R.id.crMainPfpIcon);
        pfpTxt = findViewById(R.id.crMainPfpText);

        // Home
        homeLayout = findViewById(R.id.crMainHomeContainer);
        homeChatsView = findViewById(R.id.crMainRecyclerView);
        homeChatsView.setLayoutManager(new LinearLayoutManager(this));

        // Notif
        notifLayout = findViewById(R.id.crMainNotifContainer);
        emptyNotifLayout = findViewById(R.id.crMainEmptyNotifLayout);

        // Profile
        profileLayout = findViewById(R.id.crMainProfileContainer);

        overlayLayout = findViewById(R.id.crMainOverlay);
        fetchData();
    }

    private void fetchData() {
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        // Use our new renamed method
        RetrofitClient.getApiService().tokenLogin(token).enqueue(new Callback<TokenLoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<TokenLoginResponse> call, @NonNull Response<TokenLoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // FILL THE STATIC REPOSITORY
                    GlobalData.updateData(response.body());
                    displayData();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TokenLoginResponse> call, @NonNull Throwable t) {
                Log.e("DoscordAuth", "Connection failed");
                fetchData();
            }
        });
    }

    private void displayData() {
        loadPfp(Objects.requireNonNull(GlobalData.getMe()).getPfp());
        setupRecyclerView();

        // Animate alpha from 1 to 0
        overlayLayout.animate()
                .alpha(0f)
                .setDuration(500) // 500ms
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        overlayLayout.setVisibility(View.GONE);
                    }
                });
    }

    private void loadPfp(String path) {
        String fullPath = "https://doscord-api.duckdns.org/images/" + path;
        Glide.with(this)
                .load(fullPath)
                .placeholder(R.drawable.pfp_placeholder) // Show this while it's loading
                .error(R.drawable.icon)   // Show this if the link is broken
                .centerCrop()
                .circleCrop()
                .into(pfpImg);
    }

    private void setupRecyclerView() {
        // Get the list but remove "Self" (activeUserId) so you don't chat with yourself
        List<TokenLoginResponse.User> friendsOnly = new ArrayList<>();
        List<TokenLoginResponse.User> requestsOnly = new ArrayList<>();
        List<Integer> pendingIds = GlobalData.getPendingRequestIds();
        int myId = GlobalData.getActiveUserId();

        for (TokenLoginResponse.User u : GlobalData.getUserList()) {
            if (u.getId() == myId) continue;

            // Case A: They sent ME a request (It's in the pendingIds list)
            if (pendingIds.contains(u.getId())) {
                requestsOnly.add(u);
            }
            // Case B: We are already accepted friends
            // We check the "friends_since" field (it's NULL for pending requests in our query)
            else if (u.getFriendsSince() != null) {
                friendsOnly.add(u);
            }
            // Case C: I sent THEM a request, and it's still pending
            else {
                // Optional: Add to a "Sent Requests" list or just ignore for now
                Log.d("DoscordAuth", "Outgoing request to " + u.getDisplayName() + " is still pending.");
            }
        }

        RecyclerView recyclerView = findViewById(R.id.crMainRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FriendsAdapter adapter = new FriendsAdapter(friendsOnly, this);
        recyclerView.setAdapter(adapter);

        updateRequests(requestsOnly);
    }

    private void updateRequests(List<TokenLoginResponse.User> requestsOnly) {
        RecyclerView recyclerView = findViewById(R.id.crNotifRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RequestsAdapter adapter = new RequestsAdapter(requestsOnly, this);
        recyclerView.setAdapter(adapter);
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
        // 1. Get your colors from resources
        int selectedColor = ContextCompat.getColor(this, R.color.selected);
        int unselectedColor = ContextCompat.getColor(this, R.color.unselected);

        // 2. Reset everything to unselected first
        // Icons
        homeIcon.setColorFilter(unselectedColor, PorterDuff.Mode.SRC_IN);
        notifIcon.setColorFilter(unselectedColor, PorterDuff.Mode.SRC_IN);
        pfpImg.setAlpha(0.5f);

        // Text
        homeTxt.setTextColor(unselectedColor);
        notifTxt.setTextColor(unselectedColor);
        pfpTxt.setTextColor(unselectedColor);

        // Layouts
        homeLayout.setVisibility(View.GONE);
        notifLayout.setVisibility(View.GONE);
        profileLayout.setVisibility(View.GONE);

        // 3. Highlight the one that matches the 'selected' variable
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

    public void openSettings(View v) {
        // Placeholder
        // Logout
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        // Tell the server (Don't even wait for response, just fire and forget)
        RetrofitClient.getApiService().logout(new LogoutRequest(token)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {}
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
        });
        sessionManager.logout();

        GlobalData.clear();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}