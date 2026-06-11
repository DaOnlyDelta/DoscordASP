package com.example.doscord.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doscord.R;
import com.example.doscord.activities.chatroom.CrChatActivity;
import com.example.doscord.api.BlockFriendRequest;
import com.example.doscord.api.RemoveFriendRequest;
import com.example.doscord.api.RetrofitClient;
import com.example.doscord.api.UpdateNicknameRequest;
import com.example.doscord.models.Channel;
import com.example.doscord.utils.GlobalData;
import com.example.doscord.utils.Helpers;
import com.example.doscord.utils.PfpUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    public interface UpdateListener {
        void onUpdateRequired();
    }

    private List<Channel> channelList;
    private Context context;
    private UpdateListener updateListener;

    public ChatsAdapter(List<Channel> channelList, Context context) {
        this.channelList = channelList;
        this.context = context;
    }

    public void setUpdateListener(UpdateListener listener) {
        this.updateListener = listener;
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
            String rawGroupName = channel.getGroupName();

            if (rawGroupName == null || rawGroupName.trim().isEmpty()) {
                chatDisplayName = "Empty Group";
            } else {
                // Count how many commas are in the string to see if we hit our limit of 4
                String[] parts = rawGroupName.split(", ");
                if (parts.length > 3) {
                    // Take just the first 3 names and append trailing dots
                    chatDisplayName = parts[0] + ", " + parts[1] + ", " + parts[2] + "...";
                } else {
                    // It's 1, 2, or 3 other users - display the string exactly as is!
                    chatDisplayName = rawGroupName;
                }
            }

            PfpUtils.loadGroupPfp(context, channel.getGroupPfp(), holder.pfp);
            holder.status.setVisibility(View.GONE);
        } else {
            chatDisplayName = channel.getDmDisplayNameOrNickname();
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

        holder.itemView.setOnLongClickListener(v -> {
            showBottomSheet(channel);
            return true;
        });
    }

    private void showBottomSheet(Channel channel) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_chat_options, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        /* Make the background of the bottom sheet itself transparent to show rounded corners
        View parent = (View) bottomSheetView.getParent();
        if (parent != null) {
            parent.setBackgroundResource(android.R.color.transparent);
        }*/

        ImageView pfp = bottomSheetView.findViewById(R.id.bsPfp);
        TextView displayName = bottomSheetView.findViewById(R.id.bsDisplayName);
        TextView username = bottomSheetView.findViewById(R.id.bsUsername);
        LinearLayout dmOptions = bottomSheetView.findViewById(R.id.bsDmOptions);
        LinearLayout groupOptions = bottomSheetView.findViewById(R.id.bsGroupOptions);

        if (channel.isGroup()) {
            dmOptions.setVisibility(View.GONE);
            groupOptions.setVisibility(View.VISIBLE);

            String chatDisplayName = channel.getGroupName();
            if (chatDisplayName == null || chatDisplayName.trim().isEmpty()) {
                chatDisplayName = "Empty Group";
            } else {
                String[] parts = chatDisplayName.split(", ");
                if (parts.length > 3) {
                    chatDisplayName = parts[0] + ", " + parts[1] + ", " + parts[2] + "...";
                }
            }

            displayName.setText(chatDisplayName);
            username.setVisibility(View.GONE);
            PfpUtils.loadGroupPfp(context, channel.getGroupPfp(), pfp);

            bottomSheetView.findViewById(R.id.bsLeaveGroup).setOnClickListener(v -> {
                // For now just close the bottom sheet
                bottomSheetDialog.dismiss();
            });
        } else {
            dmOptions.setVisibility(View.VISIBLE);
            groupOptions.setVisibility(View.GONE);

            displayName.setText(channel.getDmDisplayNameOrNickname());
            username.setText("@" + channel.getDmRecipientUsername());
            PfpUtils.loadPfp(context, channel.getDmRecipientPfp(), pfp);

            bottomSheetView.findViewById(R.id.bsAddNickname).setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                showNicknameDialog(channel);
            });

            bottomSheetView.findViewById(R.id.bsRemoveFriend).setOnClickListener(v -> {
                handleFriendAction(channel.getDmRecipientId(), false);
                bottomSheetDialog.dismiss();
            });

            bottomSheetView.findViewById(R.id.bsBlock).setOnClickListener(v -> {
                handleFriendAction(channel.getDmRecipientId(), true);
                bottomSheetDialog.dismiss();
            });
        }

        bottomSheetDialog.show();
    }

    private void handleFriendAction(int friendId, boolean isBlock) {
        int myId = GlobalData.getActiveUserId();
        Call<Void> call;
        if (isBlock) {
            call = RetrofitClient.getApiService().blockFriend(new BlockFriendRequest(myId, friendId));
        } else {
            call = RetrofitClient.getApiService().removeFriend(new RemoveFriendRequest(myId, friendId));
        }

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    if (updateListener != null) {
                        updateListener.onUpdateRequired();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
            }
        });
    }

    private void showNicknameDialog(Channel channel) {
        android.widget.EditText input = new android.widget.EditText(context);
        input.setHint("Enter nickname");
        input.setText(channel.getDmDisplayNameOrNickname());

        new MaterialAlertDialogBuilder(context)
                .setTitle("Set Nickname")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String nickname = input.getText().toString().trim();
                    updateNickname(channel.getDmRecipientId(), nickname);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateNickname(int friendId, String nickname) {
        int myId = GlobalData.getActiveUserId();
        RetrofitClient.getApiService().updateNickname(new UpdateNicknameRequest(myId, friendId, nickname))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            if (updateListener != null) {
                                updateListener.onUpdateRequired();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    }
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