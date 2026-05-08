package com.example.doscord.activities.chatroom;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doscord.R;
import com.example.doscord.utils.LogDataHolder;
import com.example.doscord.utils.RegDataHolder;

public class CrMainActivity extends AppCompatActivity {
    private ImageView homeIcon, notifIcon, pfpImg;
    private TextView homeTxt, notifTxt, pfpTxt;
    private int selected = 0;
    RecyclerView chatsView;

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
        homeIcon = findViewById(R.id.crMainHomeIcon);
        homeTxt = findViewById(R.id.crMainHomeText);
        notifIcon = findViewById(R.id.crMainNotifIcon);
        notifTxt = findViewById(R.id.crMainNotifText);
        pfpImg = findViewById(R.id.crMainPfpIcon);
        pfpTxt = findViewById(R.id.crMainPfpText);

        chatsView = findViewById(R.id.crMainRecyclerView);
        chatsView.setLayoutManager(new LinearLayoutManager(this));

        loadPfp();
    }

    private void loadPfp() {
        // Load pfp
        if (RegDataHolder.id != -1) {
            Glide.with(this)
                    .load((RegDataHolder.selectedImageUri != null) ? RegDataHolder.selectedImageUri : RegDataHolder.defaultPfpDrawable)
                    .centerCrop()
                    .circleCrop()
                    .into(pfpImg);
        } else {
            String fullPath = "https://doscord-api.duckdns.org/images/" + LogDataHolder.getPfp();
            Glide.with(this)
                    .load(fullPath)
                    .placeholder(R.drawable.pfp_placeholder) // Show this while it's loading
                    .error(R.drawable.icon)   // Show this if the link is broken
                    .centerCrop()
                    .circleCrop()
                    .into(pfpImg);
        }
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

        // 3. Highlight the one that matches the 'selected' variable
        switch (selected) {
            case 0:
                homeIcon.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN);
                homeTxt.setTextColor(selectedColor);
                break;
            case 1:
                notifIcon.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN);
                notifTxt.setTextColor(selectedColor);
                break;
            case 2:
                pfpImg.setAlpha(1.0f);
                pfpTxt.setTextColor(selectedColor);
                break;
        }
    }
}