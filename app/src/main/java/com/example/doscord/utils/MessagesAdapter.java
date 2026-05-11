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

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private List<Message> messagesList;
    private Context context;

    public MessagesAdapter(List<Message> messagesList, Context context) {
        this.messagesList = messagesList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messagesList.get(position);
        
        // Find sender in GlobalData.userList
        User sender = null;
        for (User u : GlobalData.getUserList()) {
            if (u.getId() == message.getSenderId()) {
                sender = u;
                break;
            }
        }

        if (sender != null) {
            String displayName = sender.getNickname();
            if (displayName == null || displayName.isEmpty()) displayName = sender.getDisplayName();
            if (displayName == null || displayName.isEmpty()) displayName = sender.getUsername();

            holder.username.setText(displayName);
            
            String pfpUrl = "https://doscord.top/api/images/" + sender.getPfp();
            Glide.with(context)
                    .load(pfpUrl)
                    .placeholder(R.drawable.pfp_placeholder)
                    .error(R.drawable.pfp_placeholder)
                    .circleCrop()
                    .into(holder.pfp);
        } else {
            holder.username.setText("Unknown User");
            holder.pfp.setImageResource(R.drawable.pfp_placeholder);
        }

        holder.content.setText(message.getMessageText());
        holder.time.setText(Helpers.formatTime(message.getSentAt()));
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView pfp;
        TextView username, content, time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pfp = itemView.findViewById(R.id.itemMsgPfp);
            username = itemView.findViewById(R.id.itemMsgUsername);
            content = itemView.findViewById(R.id.itemMsgContent);
            time = itemView.findViewById(R.id.itemMsgTime);
        }
    }
}
