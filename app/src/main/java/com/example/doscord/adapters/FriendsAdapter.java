package com.example.doscord.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doscord.R;
import com.example.doscord.models.User;
import com.example.doscord.utils.PfpUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<FriendListItem> items;
    private final Context context;

    public static abstract class FriendListItem {
        public static final int TYPE_HEADER = 0;
        public static final int TYPE_FRIEND_GROUP = 1;
        abstract int getType();
    }

    public static class HeaderItem extends FriendListItem {
        private final String letter;
        public HeaderItem(String letter) { this.letter = letter; }
        public String getLetter() { return letter; }
        @Override int getType() { return TYPE_HEADER; }
    }

    public static class FriendGroupItem extends FriendListItem {
        private final List<User> users;
        public FriendGroupItem(List<User> users) { this.users = users; }
        public List<User> getUsers() { return users; }
        @Override int getType() { return TYPE_FRIEND_GROUP; }
    }

    public FriendsAdapter(List<User> friends, Context context) {
        this.context = context;
        this.items = processFriends(friends);
    }

    private List<FriendListItem> processFriends(List<User> friends) {
        List<FriendListItem> processed = new ArrayList<>();
        if (friends == null || friends.isEmpty()) return processed;

        List<User> sortedFriends = new ArrayList<>(friends);
        sortedFriends.sort((u1, u2) -> {
            String name1 = u1.getDisplayName() != null ? u1.getDisplayName() : u1.getUsername();
            String name2 = u2.getDisplayName() != null ? u2.getDisplayName() : u2.getUsername();
            if (name1 == null) name1 = "";
            if (name2 == null) name2 = "";
            return name1.compareToIgnoreCase(name2);
        });

        String currentLetter = "";
        List<User> currentGroup = new ArrayList<>();
        for (User user : sortedFriends) {
            String name = user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
            if (name == null || name.isEmpty()) continue;
            
            String firstLetter = name.substring(0, 1).toUpperCase();
            if (!firstLetter.equals(currentLetter)) {
                if (!currentGroup.isEmpty()) {
                    processed.add(new FriendGroupItem(new ArrayList<>(currentGroup)));
                    currentGroup.clear();
                }
                currentLetter = firstLetter;
                processed.add(new HeaderItem(currentLetter));
            }
            currentGroup.add(user);
        }
        if (!currentGroup.isEmpty()) {
            processed.add(new FriendGroupItem(currentGroup));
        }
        return processed;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == FriendListItem.TYPE_HEADER) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_friend_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
            return new FriendGroupViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FriendListItem item = items.get(position);
        if (item.getType() == FriendListItem.TYPE_HEADER) {
            HeaderViewHolder h = (HeaderViewHolder) holder;
            h.header.setText(((HeaderItem) item).getLetter());
        } else {
            FriendGroupViewHolder f = (FriendGroupViewHolder) holder;
            List<User> users = ((FriendGroupItem) item).getUsers();
            
            f.container.removeAllViews();
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                View row = LayoutInflater.from(context).inflate(R.layout.item_friend_row, f.container, false);
                
                ImageView pfp = row.findViewById(R.id.itemFriendPfp);
                TextView displayName = row.findViewById(R.id.itemFriendDisplayName);
                TextView username = row.findViewById(R.id.itemFriendUsername);
                MaterialCardView status = row.findViewById(R.id.itemFriendStatus);
                
                displayName.setText(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
                username.setText(user.getUsername());
                PfpUtils.loadPfp(context, user.getPfp(), pfp);
                
                if (user.isOnline()) {
                    status.setCardBackgroundColor(context.getColor(R.color.green));
                } else {
                    status.setCardBackgroundColor(context.getColor(R.color.gray));
                }
                
                f.container.addView(row);
                
                if (i < users.size() - 1) {
                    View divider = new View(context);
                    float density = context.getResources().getDisplayMetrics().density;
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (1 * density));
                    params.setMargins((int) (65 * density), 0, 0, 0); // Align divider with text
                    divider.setLayoutParams(params);
                    divider.setBackgroundColor(Color.parseColor("#2c2d33"));
                    f.container.addView(divider);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView header;
        HeaderViewHolder(View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.itemFriendHeader);
        }
    }

    static class FriendGroupViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;

        FriendGroupViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.itemFriendGroupContainer);
        }
    }
}