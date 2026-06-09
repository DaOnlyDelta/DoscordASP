package com.example.doscord.adapters;

import android.content.Context;
import android.graphics.Color;
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
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupSelectionAdapter extends RecyclerView.Adapter<GroupSelectionAdapter.ViewHolder> {

    private List<User> users;
    private final Context context;
    private final Set<Integer> selectedUserIds = new HashSet<>();
    private final OnSelectionChangedListener listener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int count);
    }

    public GroupSelectionAdapter(List<User> users, Context context, OnSelectionChangedListener listener) {
        this.users = new ArrayList<>(users);
        this.context = context;
        this.listener = listener;
        sortUsers(this.users);
    }

    private void sortUsers(List<User> list) {
        list.sort((u1, u2) -> {
            String name1 = u1.getDisplayName() != null ? u1.getDisplayName() : u1.getUsername();
            String name2 = u2.getDisplayName() != null ? u2.getDisplayName() : u2.getUsername();
            if (name1 == null) name1 = "";
            if (name2 == null) name2 = "";
            return name1.compareToIgnoreCase(name2);
        });
    }

    public void updateList(List<User> newList) {
        this.users = new ArrayList<>(newList);
        sortUsers(this.users);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_group_select_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.displayName.setText(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
        holder.username.setText(user.getUsername());
        PfpUtils.loadPfp(context, user.getPfp(), holder.pfp);

        boolean isSelected = selectedUserIds.contains(user.getId());
        updateSelectionUI(holder, isSelected);

        holder.itemView.setOnClickListener(v -> {
            if (selectedUserIds.contains(user.getId())) {
                selectedUserIds.remove(user.getId());
            } else {
                if (selectedUserIds.size() < 10) {
                    selectedUserIds.add(user.getId());
                }
            }
            notifyItemChanged(position);
            if (listener != null) {
                listener.onSelectionChanged(selectedUserIds.size());
            }
        });

        // Divider logic
        holder.divider.setVisibility(position < users.size() - 1 ? View.VISIBLE : View.GONE);
    }

    private void updateSelectionUI(ViewHolder holder, boolean isSelected) {
        if (isSelected) {
            holder.checkboxCard.setCardBackgroundColor(context.getColor(R.color.blue));
            holder.checkboxCard.setStrokeWidth(0);
            holder.checkMark.setVisibility(View.VISIBLE);
        } else {
            holder.checkboxCard.setCardBackgroundColor(Color.TRANSPARENT);
            holder.checkboxCard.setStrokeWidth((int) (2 * context.getResources().getDisplayMetrics().density));
            holder.checkboxCard.setStrokeColor(context.getColor(R.color.textGray));
            holder.checkMark.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public List<Integer> getSelectedUserIds() {
        return new ArrayList<>(selectedUserIds);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView pfp, checkMark;
        TextView displayName, username;
        MaterialCardView checkboxCard;
        View divider;

        ViewHolder(View itemView) {
            super(itemView);
            pfp = itemView.findViewById(R.id.itemGroupSelectPfp);
            displayName = itemView.findViewById(R.id.itemGroupSelectDisplayName);
            username = itemView.findViewById(R.id.itemGroupSelectUsername);
            checkboxCard = itemView.findViewById(R.id.itemGroupSelectCard);
            checkMark = itemView.findViewById(R.id.itemGroupSelectCheck);
            divider = itemView.findViewById(R.id.itemGroupSelectDivider);
        }
    }
}