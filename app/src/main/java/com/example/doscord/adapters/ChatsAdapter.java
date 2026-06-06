package com.example.doscord.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doscord.R;
import com.example.doscord.activities.chatroom.CrChatActivity;
import com.example.doscord.models.Channel;
import com.example.doscord.utils.GlobalData;
import com.example.doscord.utils.Helpers;
import com.example.doscord.utils.PfpUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private List<Channel> channelList;
    private Context context;

    public ChatsAdapter(List<Channel> channelList, Context context) {
        this.channelList = channelList;
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
        Channel channel = channelList.get(position);

        String chatDisplayName;

        // 1. Differentiate between Group Chat and 1-to-1 DM
        if (channel.isGroup()) {
            chatDisplayName = channel.getGroupName() != null ? channel.getGroupName() : "Group Chat";
            PfpUtils.loadGroupPfp(context, channel.getGroupPfp(), holder.pfp);

            // Groups don't use a single online indicator ring on the channel icon
            holder.status.setVisibility(View.GONE);
        } else {
            chatDisplayName = channel.getDmDisplayNameOrNickname();
            if (chatDisplayName == null || chatDisplayName.isEmpty()) {
                chatDisplayName = channel.getDmRecipientUsername();
            }
            PfpUtils.loadPfp(context, channel.getDmRecipientPfp(), holder.pfp);

            // Show online indicator ring for direct messages
            holder.status.setVisibility(View.VISIBLE);
            if (channel.isDmRecipientOnline()) {
                holder.status.setCardBackgroundColor(ContextCompat.getColor(context, R.color.green));
            } else {
                holder.status.setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray));
            }
        }

        // Set the final calculated title
        holder.name.setText(chatDisplayName);

        // 2. Set Last Message text with clean sender prefixes
        if (channel.getLastMessage() != null) {
            String prefix = "";
            if (channel.getLastMessageSenderId() != null) {
                if (channel.getLastMessageSenderId().equals(GlobalData.getActiveUserId())) {
                    prefix = "You: ";
                } else if (!channel.isGroup()) {
                    // For DMs, use the sender's display name or nickname
                    prefix = chatDisplayName + ": ";
                } else {
                    // For Group chats, we don't have the sender's real name inside this payload yet,
                    // so we can leave it blank or handle it dynamically later.
                    prefix = "";
                }
            }
            holder.message.setText(prefix + channel.getLastMessage());
        } else {
            holder.message.setText("No messages yet. Say hi!");
        }

        // 3. Format and assign transmission timestamp
        if (channel.getLastMessageTime() != null) {
            holder.time.setText(Helpers.formatTime(channel.getLastMessageTime()));
        } else {
            holder.time.setText("");
        }

        // 4. Fire Navigation Explicit Intent to Chat Activity
        String finalChatDisplayName = chatDisplayName;
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CrChatActivity.class);
            intent.putExtra("channel_id", channel.getChannelId());
            intent.putExtra("chat_name", finalChatDisplayName);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return channelList != null ? channelList.size() : 0;
    }

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