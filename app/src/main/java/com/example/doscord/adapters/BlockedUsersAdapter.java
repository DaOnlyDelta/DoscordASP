package com.example.doscord.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doscord.R;
import com.example.doscord.models.User;
import com.example.doscord.utils.PfpUtils;

import java.util.List;

public class BlockedUsersAdapter extends RecyclerView.Adapter<BlockedUsersAdapter.ViewHolder> {

    private List<User> blockedUsers;
    private final Context context;
    private OnUnblockClickListener listener;

    public interface OnUnblockClickListener {
        void onUnblockClick(User user);
    }

    public BlockedUsersAdapter(List<User> blockedUsers, Context context, OnUnblockClickListener listener) {
        this.blockedUsers = blockedUsers;
        this.context = context;
        this.listener = listener;
    }

    public void updateList(List<User> newList) {
        this.blockedUsers = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_blocked_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = blockedUsers.get(position);

        holder.displayName.setText(user.getNameToDisplay());
        holder.username.setText(user.getUsername());
        PfpUtils.loadPfp(context, user.getPfp(), holder.pfp);

        holder.unblockBtnContainer.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUnblockClick(user);
            }
        });

        // As requested: clicking on the boxes shouldn't do anything
        holder.itemView.setOnClickListener(null);
        holder.itemView.setClickable(false);
    }

    @Override
    public int getItemCount() {
        return blockedUsers != null ? blockedUsers.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView pfp, unblockBtn;
        View unblockBtnContainer;
        TextView displayName, username;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pfp = itemView.findViewById(R.id.itemBlockedPfp);
            displayName = itemView.findViewById(R.id.itemBlockedDisplayName);
            username = itemView.findViewById(R.id.itemBlockedUsername);
            unblockBtn = itemView.findViewById(R.id.itemBlockedUnblockBtn);
            unblockBtnContainer = itemView.findViewById(R.id.itemBlockedUnblockBtnContainer);
        }
    }
}