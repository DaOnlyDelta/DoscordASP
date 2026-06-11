package com.example.doscord.activities.chatroom;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.doscord.api.UpdateUserRequest;
import com.example.doscord.api.UpdateUserResponse;
import com.example.doscord.adapters.ChatsAdapter;
import com.example.doscord.models.Channel;
import com.example.doscord.utils.GlobalData;
import com.example.doscord.utils.Helpers;
import com.example.doscord.utils.LogDataHolder;
import com.example.doscord.utils.PfpUtils;
import com.example.doscord.utils.RegDataHolder;
import com.example.doscord.models.User;
import com.example.doscord.adapters.RequestsAdapter;
import com.example.doscord.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrMainActivity extends AppCompatActivity {
    private ImageView homeIcon, notifIcon, pfpImg, profileIcon;
    private TextView homeTxt, notifTxt, pfpTxt, profileDisplayName, profileUsername, profileCreatedAt, profileNFriends, profileNBlocked, editIdentifierLabel;
    private TextView editIdentifierWarning;
    private EditText editDisplayNameInput, editIdentifierInput;
    private Button editSaveBtn;
    private int selected = 0;
    private RecyclerView homeChatsView;
    private MaterialCardView profileEditContainer, profilePlusContainer;
    private ConstraintLayout homeLayout, notifLayout, emptyNotifLayout, profileLayout, overlayLayout, profileDataLayout;
    private int attempt = 0;
    private boolean editable = false;

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
        profileUsername = findViewById(R.id.crMainProfileUsernameTxt);
        profileDisplayName = findViewById(R.id.crMainProfileDisplayNameTxt);
        profileDataLayout = findViewById(R.id.crMainProfileDataContainer);
        profileEditContainer = findViewById(R.id.crMainProfileEditContainer);
        profileCreatedAt = findViewById(R.id.crMainProfileMemberSinceTxt);
        profileNFriends = findViewById(R.id.crMainProfileNFriendsTxt);
        profileNBlocked = findViewById(R.id.crMainProfileBlockedTxt);

        editDisplayNameInput = findViewById(R.id.crMainProfileEditDisplayNameInput);
        editIdentifierInput = findViewById(R.id.crMainProfileEditIdentifierInput);
        editIdentifierLabel = findViewById(R.id.crMainProfileEditIdentifierLabel);
        profilePlusContainer = findViewById(R.id.crMainProfilePlusContainer);
        editIdentifierWarning = findViewById(R.id.crMainProfileEditIdentifierWarning);
        editSaveBtn = findViewById(R.id.crMainProfileEditSaveBtn);

        overlayLayout = findViewById(R.id.crMainOverlay);
        setupTextListener();
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
            String atUser = "@" + me.getUsername();
            profileUsername.setText(atUser);
            profileDisplayName.setText((me.getDisplayName() != null) ? me.getDisplayName() : me.getUsername());
            profileCreatedAt.setText(Helpers.formatDate(me.getCreated_at()));
            profileNFriends.setText(String.valueOf(GlobalData.getFriends().size()));
            profileNBlocked.setText(String.valueOf(GlobalData.getBlocked().size()));

            // Populate edit fields if not currently editing to avoid overwriting user input
            if (!editable) {
                editDisplayNameInput.setText(me.getDisplayName() != null ? me.getDisplayName() : "");

                if (me.getEmail() != null && !me.getEmail().isEmpty()) {
                    editIdentifierLabel.setText("Email");
                    editIdentifierInput.setText(me.getEmail());
                } else if (me.getPhone() != null && !me.getPhone().isEmpty()) {
                    editIdentifierLabel.setText("Phone Number");
                    editIdentifierInput.setText(me.getPhone());
                }
            }

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
        adapter.setUpdateListener(this::fetchData);
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

    public void openBlockedUsers(View v) {
        Intent intent = new Intent(this, CrBlockListActivity.class);
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
        if (!editable) return;
        Intent intent = new Intent(this, RegPfpActivity.class);
        startActivity(intent);
    }

    public void newChat(View v) {
        Intent intent = new Intent(this, CrNewChatActivity.class);
        startActivity(intent);
    }

    private void setupTextListener() {
        editIdentifierInput.addTextChangedListener(new TextWatcher(){
            @Override
            public void afterTextChanged(Editable editable) {}

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                editIdentifierWarning.setVisibility(View.GONE);
                if (charSequence.length() == 0) {
                    editSaveBtn.setEnabled(false);
                    editSaveBtn.setAlpha(0.5f);
                } else {
                    editSaveBtn.setEnabled(true);
                    editSaveBtn.setAlpha(1.0f);
                }
            }
        });
    }

    public void editProfile(View v) {
        editable = true;
        profileDataLayout.setVisibility(View.GONE);
        profilePlusContainer.setVisibility(View.VISIBLE);
        profileEditContainer.setVisibility(View.VISIBLE);
    }

    public void saveProfileChanges(View v) {
        User me = GlobalData.getMyProfile();
        if (me == null) return;

        String newDisplayName = editDisplayNameInput.getText().toString().trim();
        String newIdentifier = editIdentifierInput.getText().toString().trim();

        if (newIdentifier.equals(me.getEmail()) || newIdentifier.equals(me.getPhone())) {
            if (newDisplayName.equals(me.getDisplayName())) {
                editable = false;
                profileDataLayout.setVisibility(View.VISIBLE);
                profilePlusContainer.setVisibility(View.GONE);
                profileEditContainer.setVisibility(View.GONE);
                return;
            }
        }

        // Double check email validity if in email mode
        if (me.getEmail() != null && !me.getEmail().isEmpty()) {
            if (!Helpers.isValidEmail(newIdentifier)) {
                editIdentifierWarning.setVisibility(View.VISIBLE);
                editIdentifierWarning.setText(R.string.please_enter_a_valid_email_address);
                editIdentifierWarning.setTextColor(ContextCompat.getColor(this, R.color.red));
                return;
            }
        }

        String token = new SessionManager(this).getToken();
        String email = null;
        String phone = null;

        if (me.getEmail() != null && !me.getEmail().isEmpty()) {
            email = newIdentifier;
        } else {
            phone = newIdentifier;
        }

        editSaveBtn.setEnabled(false);
        editSaveBtn.setAlpha(0.5f);

        RetrofitClient.getApiService().updateUser(new UpdateUserRequest(token, newDisplayName, email, phone))
                .enqueue(new Callback<UpdateUserResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UpdateUserResponse> call, @NonNull Response<UpdateUserResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            int code = response.body().getErrorCode();
                            if (code == 0) {
                                editable = false;
                                profileEditContainer.setVisibility(View.GONE);
                                profileDataLayout.setVisibility(View.VISIBLE);
                                fetchData(); // Refresh data to show changes
                            } else if (code == 1) {
                                String type = (me.getEmail() != null && !me.getEmail().isEmpty()) ? "email" : "phone number";
                                editIdentifierWarning.setText("This " + type + " is already taken!");
                                editIdentifierWarning.setTextColor(ContextCompat.getColor(CrMainActivity.this, R.color.red));
                                editIdentifierWarning.setVisibility(View.VISIBLE);
                            } else {
                                editIdentifierWarning.setText("Update failed");
                                editIdentifierWarning.setVisibility(View.VISIBLE);
                            }
                        } else {
                            editIdentifierWarning.setText("Server error");
                            editIdentifierWarning.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UpdateUserResponse> call, @NonNull Throwable t) {
                        editIdentifierWarning.setText("Network error");
                        editIdentifierWarning.setVisibility(View.VISIBLE);
                    }
                });
    }
}