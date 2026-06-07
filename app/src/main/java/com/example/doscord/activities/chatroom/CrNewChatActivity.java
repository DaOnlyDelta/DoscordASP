package com.example.doscord.activities.chatroom;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doscord.R;
import com.example.doscord.adapters.FriendsAdapter;
import com.example.doscord.models.User;
import com.example.doscord.utils.GlobalData;

import java.util.ArrayList;
import java.util.List;

public class CrNewChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FriendsAdapter adapter;
    private EditText searchEdit;
    private List<User> allFriends = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cr_new_chat);
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
        recyclerView = findViewById(R.id.crNewChatRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchEdit = findViewById(R.id.crNewChatSearchEdit);
    }

    private void loadFriends() {
        allFriends = GlobalData.getFriends();
        
        adapter = new FriendsAdapter(allFriends, this);
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
            adapter = new FriendsAdapter(allFriends, this);
        } else {
            List<User> filtered = new ArrayList<>();
            for (User user : allFriends) {
                String displayName = user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
                if ((displayName != null && displayName.toLowerCase().contains(query.toLowerCase())) ||
                    (user.getUsername() != null && user.getUsername().toLowerCase().contains(query.toLowerCase()))) {
                    filtered.add(user);
                }
            }
            adapter = new FriendsAdapter(filtered, this);
        }
        recyclerView.setAdapter(adapter);
    }

    public void finish(View v) {
        finish();
    }

    public void createGroup(View v) {
        // TODO: Implement create group activity/dialog
    }

    public void addFriend(View v) {
        Intent intent = new Intent(this, AddFriendsActivity.class);
        startActivity(intent);
    }
}