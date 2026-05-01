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

public class ChatroomActivity extends AppCompatActivity {

    private ImageView pfpImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chatroom);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.crPfpImg), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
    }

    private void initViews() {
        pfpImg = findViewById(R.id.crPfpImg);

        // Load pfp
        String pfpPath = LogDataHolder.getPfp();
        // Check for both null and empty
        if (pfpPath == null || pfpPath.isEmpty()) {
            pfpPath = "defaults/defaults0.png";
        }

        String fullPfpUrl = "https://doscord-api.duckdns.org/images/" + pfpPath;
        Glide.with(this)
                .load(fullPfpUrl)
                .circleCrop() // Makes it look like Discord
                .placeholder(R.drawable.pfp_placeholder) // Show while loading
                .into(pfpImg);
    }
}