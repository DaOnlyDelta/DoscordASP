package com.example.doscord.activities.chatroom;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doscord.R;
import com.example.doscord.adapters.GroupSelectionAdapter;
import com.example.doscord.api.AddMembersRequest;
import com.example.doscord.api.CreateGroupRequest;
import com.example.doscord.api.CreateGroupResponse;
import com.example.doscord.api.RetrofitClient;
import com.example.doscord.models.Channel;
import com.example.doscord.models.User;
import com.example.doscord.utils.GlobalData;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrNewGroupActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GroupSelectionAdapter adapter;
    private EditText searchEdit;
    private TextView subtitle, createBtn, title;
    private List<User> allFriends = new ArrayList<>();

    // Mode configuration variables
    private boolean isAddingMembers = false;
    private int currentChannelId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cr_new_group);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Process mode arguments passed from the MessagesAdapter intent
        if (getIntent() != null) {
            isAddingMembers = getIntent().getBooleanExtra("isAddingMembers", false);
            currentChannelId = getIntent().getIntExtra("channelId", -1);
        }

        initViews();
        loadFriends();
        setupSearch();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.crNewGroupRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchEdit = findViewById(R.id.crNewGroupSearchEdit);
        subtitle = findViewById(R.id.crNewGroupSubtitle);
        createBtn = findViewById(R.id.crNewGroupCreateBtn);
        title = findViewById(R.id.crNewGroupTitle); // Grab standard toolbar header text view if available

        // 2. Adjust dynamic UI text based on mode parameters
        if (isAddingMembers) {
            createBtn.setText("Add");
            if (title != null) title.setText("Add Members");
        } else {
            createBtn.setText("Create");
        }

        createBtn.setOnClickListener(v -> {
            if (isAddingMembers) {
                sendAddMembersRequest();
            } else {
                sendCreateGroupRequest();
            }
        });
    }

    private void sendCreateGroupRequest() {
        // Fallback for standard creation mode tracking all IDs
        List<Integer> selectedIds = adapter.getNewSelectedUserIds();
        int creatorId = GlobalData.getActiveUserId();

        CreateGroupRequest request = new CreateGroupRequest(creatorId, selectedIds);

        RetrofitClient.getApiService().createGroup(request).enqueue(new Callback<CreateGroupResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateGroupResponse> call, @NonNull Response<CreateGroupResponse> response) {
                if (response.isSuccessful()) {
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreateGroupResponse> call, @NonNull Throwable t) {
                // Network failure handling
            }
        });
    }

    private void sendAddMembersRequest() {
        // 3. Gather only the newly checked user ids
        List<Integer> newMembersToAdd = adapter.getNewSelectedUserIds();
        if (newMembersToAdd.isEmpty()) {
            finish();
            return;
        }
        int id = GlobalData.getActiveUserId();

        // Create the network payload container
        AddMembersRequest request = new AddMembersRequest(currentChannelId, newMembersToAdd, id);

        // Enqueue the network call asynchronously expecting a Void response body
        RetrofitClient.getApiService().addMembersToGroup(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                // Check for a standard 2xx success status code
                if (response.isSuccessful()) {
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                // Network pipeline failure handling
                t.printStackTrace();
            }
        });
    }

    private void loadFriends() {
        allFriends = GlobalData.getFriends();

        adapter = new GroupSelectionAdapter(allFriends, this, count -> {
            String subTxt = count + " of 10 members";
            subtitle.setText(subTxt);
            createBtn.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        });
        recyclerView.setAdapter(adapter);

        // 4. If modifying an existing group, locate and pass existing member IDs
        if (isAddingMembers && currentChannelId != -1) {
            List<Integer> existingIds = new ArrayList<>();
            Channel currentGroupChannel = null;

            for (Channel channel : GlobalData.getChannelList()) {
                if (channel.getChannelId() == currentChannelId) {
                    currentGroupChannel = channel;
                    break;
                }
            }

            if (currentGroupChannel != null && currentGroupChannel.getGroupName() != null) {
                String[] names = currentGroupChannel.getGroupName().split(", ");

                // Match names against friend list to collect corresponding IDs
                for (String name : names) {
                    for (User user : allFriends) {
                        String matchName = user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
                        if (matchName != null && matchName.equalsIgnoreCase(name)) {
                            existingIds.add(user.getId());
                        }
                    }
                }
            }

            // Always ensure the logged-in admin user is added to the immutable set
            existingIds.add(GlobalData.getActiveUserId());

            // Apply restrictions to the adapter
            adapter.setExistingMembers(existingIds);
        }
    }

    private void setupSearch() {
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFriends(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterFriends(String query) {
        if (query.isEmpty()) {
            adapter.updateList(allFriends);
        } else {
            List<User> filtered = new ArrayList<>();
            for (User user : allFriends) {
                String displayName = user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
                if ((displayName != null && displayName.toLowerCase().contains(query.toLowerCase())) ||
                        (user.getUsername() != null && user.getUsername().toLowerCase().contains(query.toLowerCase()))) {
                    filtered.add(user);
                }
            }
            adapter.updateList(filtered);
        }
    }

    public void finish(View v) {
        finish();
    }
}