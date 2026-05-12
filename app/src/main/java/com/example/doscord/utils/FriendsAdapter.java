package com.example.doscord.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doscord.R;
import com.example.doscord.activities.chatroom.CrChatActivity;
import com.example.doscord.activities.menu.RegUserActivity;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private List<User> friendsList;
    private Context context;

    public FriendsAdapter(List<User> friendsList, Context context) {
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
        User user = friendsList.get(position);

        // 1. Set the Name (SQL already handles picking the right nickname)
        String finalDisplayName = user.getUsername();
        if (user.getNickname() != null && !user.getNickname().isEmpty()) {
            finalDisplayName = user.getNickname();
        } else if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            finalDisplayName = user.getDisplayName();
        }
        holder.name.setText(finalDisplayName);

        // 2. Set Last Message + Sender Prefix
        if (user.getLastMessage() != null) {
            String prefix = "";
            // Check if the sender ID matches the Active User (You)
            if (user.getLastMessageSenderId() != null) {
                if (user.getLastMessageSenderId() == GlobalData.getActiveUserId()) {
                    prefix = "You: ";
                } else {
                    // It was sent by the friend.
                    // You can use their nickname/display name or just leave it blank.
                    prefix = finalDisplayName + ": ";
                }
            }
            holder.message.setText(prefix + user.getLastMessage());
        } else {
            holder.message.setText("No messages yet. Say hi!");
        }

        // 3. Format and Set Time
        if (user.getLastMessageTime() != null) {
            holder.time.setText(Helpers.formatTime(user.getLastMessageTime()));
        } else {
            holder.time.setText("");
        }

        // 4. Load PFP
        String pfpUrl = "https://doscord.top/api/images/" + user.getPfp();
        Glide.with(context)
                .load(pfpUrl)
                .placeholder(R.drawable.pfp_placeholder)
                .error(R.drawable.pfp_placeholder) // Fallback if image fails
                .circleCrop() // Optional: makes PFPs round
                .into(holder.pfp);

        // Update their status
        if (user.isOnline()) {
            holder.status.setCardBackgroundColor(ContextCompat.getColor(context, R.color.green));
        } else {
            holder.status.setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray));
        }

        // 6. Click Listener
        final String chatDisplayName = finalDisplayName;
        holder.itemView.setOnClickListener(v -> {
             Intent intent = new Intent(context, CrChatActivity.class);
             intent.putExtra("channel_id", user.getChannelId());
             intent.putExtra("chat_name", chatDisplayName);
             context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return friendsList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView pfp;
        TextView name, message, time;
        MaterialCardView status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pfp = itemView.findViewById(R.id.itemChatPfp);
            name = itemView.findViewById(R.id.itemChatName);
            message = itemView.findViewById(R.id.itemChatMessage);
            time = itemView.findViewById(R.id.itemChatTime);
            status = itemView.findViewById(R.id.itemChatStatus);
        }
    }
}
