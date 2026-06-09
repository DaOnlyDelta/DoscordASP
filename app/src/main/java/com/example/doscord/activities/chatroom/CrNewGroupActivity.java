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
import com.example.doscord.api.CreateGroupRequest;
import com.example.doscord.api.CreateGroupResponse;
import com.example.doscord.api.RetrofitClient;
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
    private TextView subtitle, createBtn;
    private List<User> allFriends = new ArrayList<>();

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

        createBtn.setOnClickListener(v -> {
            sendCreateGroupRequest();
        });
    }

    private void sendCreateGroupRequest() {
        List<Integer> selectedIds = adapter.getSelectedUserIds();
        int creatorId = GlobalData.getActiveUserId();

        // Pass null for name as requested
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
                // Future: Handle network failure
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