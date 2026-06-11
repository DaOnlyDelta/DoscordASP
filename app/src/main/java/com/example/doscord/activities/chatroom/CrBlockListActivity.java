package com.example.doscord.activities.chatroom;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doscord.R;
import com.example.doscord.adapters.BlockedUsersAdapter;
import com.example.doscord.api.RetrofitClient;
import com.example.doscord.api.UnblockUserRequest;
import com.example.doscord.models.User;
import com.example.doscord.utils.GlobalData;

import java.util.ArrayList;
import java.util.List;

public class CrBlockListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BlockedUsersAdapter adapter;
    private EditText searchEdit;
    private List<User> allBlocked = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cr_block_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        loadBlockedUsers();
        setupSearch();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.crBlockListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchEdit = findViewById(R.id.crBlockListSearchEdit);
    }

    private void loadBlockedUsers() {
        allBlocked = GlobalData.getBlocked();
        adapter = new BlockedUsersAdapter(allBlocked, this, user -> {
            // Unblock logic will go here
            unblockUser(user);
        });
        recyclerView.setAdapter(adapter);
    }

    private void unblockUser(User user) {
        int myId = GlobalData.getActiveUserId();
        int blockedId = user.getId();

        UnblockUserRequest req = new UnblockUserRequest(myId, blockedId);
        RetrofitClient.getApiService().unblock(req).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<Void> call, @NonNull retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    // Refresh data after unblocking
                    fetchData();
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<Void> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void fetchData() {
        String token = new com.example.doscord.utils.SessionManager(this).getToken();
        RetrofitClient.getApiService().tokenLogin(token).enqueue(new retrofit2.Callback<com.example.doscord.api.TokenLoginResponse>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<com.example.doscord.api.TokenLoginResponse> call, @NonNull retrofit2.Response<com.example.doscord.api.TokenLoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GlobalData.updateData(response.body());
                    loadBlockedUsers(); // Reload UI with updated data
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<com.example.doscord.api.TokenLoginResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void setupSearch() {
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBlocked(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterBlocked(String query) {
        if (query.isEmpty()) {
            adapter.updateList(allBlocked);
        } else {
            List<User> filtered = new ArrayList<>();
            for (User user : allBlocked) {
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