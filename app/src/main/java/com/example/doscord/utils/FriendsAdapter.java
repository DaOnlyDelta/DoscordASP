package com.example.doscord.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doscord.R;
import com.example.doscord.api.TokenLoginResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private List<TokenLoginResponse.User> friendsList;
    private Context context;

    public FriendsAdapter(List<TokenLoginResponse.User> friendsList, Context context) {
        this.friendsList = friendsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TokenLoginResponse.User user = friendsList.get(position);

        // 1. Set the Name (Use Nickname if it exists, otherwise Display Name)
        String displayName = user.getUsername();
        if (user.getNickname() != null && !user.getNickname().isEmpty()) {
            displayName = user.getNickname();
        } else if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            displayName = user.getDisplayName();
        }
        holder.name.setText(displayName);

        // 2. Set Last Message (Handle null if you've never chatted)
        if (user.getLastMessage() != null) {
            holder.message.setText(user.getLastMessage());
        } else {
            holder.message.setText("No messages yet. Say hi!");
        }

        // 3. Loading time
        holder.time.setText(formatTime(user.getLastMessageTime()));

        // 4. Load PFP using Glide
        String pfpUrl = "https://doscord-api.duckdns.org/images/" + user.getPfp();
        Glide.with(context)
                .load(pfpUrl)
                .placeholder(R.drawable.pfp_placeholder)
                .into(holder.pfp);

        // 5. Click Listener to open the Chat
        holder.itemView.setOnClickListener(v -> {
            // Intent intent = new Intent(context, ChatActivity.class);
            // intent.putExtra("friend_id", user.getId());
            // context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return friendsList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView pfp;
        TextView name, message, time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pfp = itemView.findViewById(R.id.itemChatPfp);
            name = itemView.findViewById(R.id.itemChatName);
            message = itemView.findViewById(R.id.itemChatMessage);
            time = itemView.findViewById(R.id.itemChatTime);
        }
    }

    public String formatTime(String rawTime) {
        if (rawTime == null || rawTime.isEmpty()) return "";

        try {
            // 1. Parse the DB string (Adjust format if your DB sends T or .000Z)
            // Since you used dateStrings: true, it should be yyyy-MM-dd HH:mm:ss
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date messageDate = sdf.parse(rawTime);
            assert messageDate != null;
            long messageTime = messageDate.getTime();
            long now = System.currentTimeMillis();

            // 2. Calculate difference in seconds
            long diffSeconds = (now - messageTime) / 1000;

            if (diffSeconds < 60) return "1m"; // Discord starts at 1m

            long diffMinutes = diffSeconds / 60;
            if (diffMinutes < 60) return diffMinutes + "m";

            long diffHours = diffMinutes / 60;
            if (diffHours < 24) return diffHours + "h";

            long diffDays = diffHours / 24;
            if (diffDays < 30) return diffDays + "d";

            long diffMonths = diffDays / 30;
            if (diffMonths < 12) return diffMonths + "mo";

            long diffYears = diffMonths / 12;
            return diffYears + "y";

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
