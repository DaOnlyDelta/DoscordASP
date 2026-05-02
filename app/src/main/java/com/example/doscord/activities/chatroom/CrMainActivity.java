package com.example.doscord.activities.chatroom;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.doscord.R;
import com.example.doscord.utils.LogDataHolder;
import com.example.doscord.utils.RegDataHolder;

public class CrMainActivity extends AppCompatActivity {
    private ImageView pfpImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cr_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        initViews();
    }

    private void initViews() {
        pfpImg = findViewById(R.id.crMainPfpBtn);

        loadPfp();
    }

    private void loadPfp() {
        // Load pfp
        if (RegDataHolder.id != -1) {
            Glide.with(this)
                    .load((RegDataHolder.selectedImageUri != null) ? RegDataHolder.selectedImageUri : RegDataHolder.defaultPfpDrawable)
                    .circleCrop()
                    .into(pfpImg);
        } else {
            String fullPath = "https://doscord-api.duckdns.org/images/" + LogDataHolder.getPfp();
            Glide.with(this)
                    .load(fullPath)
                    .placeholder(R.drawable.pfp_placeholder) // Show this while it's loading
                    .error(R.drawable.icon)   // Show this if the link is broken
                    .circleCrop()
                    .into(pfpImg);
        }


    }
}