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

import java.util.List;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {

    private List<TokenLoginResponse.User> requestList;
    private Context context;

    public RequestsAdapter(List<TokenLoginResponse.User> requestList, Context context) {
        this.requestList = requestList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TokenLoginResponse.User user = requestList.get(position);

        String displayName = user.getUsername();
        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            displayName = user.getDisplayName();
        }
        String info = displayName + context.getString(R.string.sent_you_a_friend_request);
        holder.name.setText(info);

        holder.time.setText(Helpers.formatTime(user.getFriendsSince()));

        // Load PFP
        Glide.with(context)
                .load("https://doscord-api.duckdns.org/images/" + user.getPfp())
                .placeholder(R.drawable.pfp_placeholder)
                .circleCrop()
                .into(holder.pfp);

        // Green Check Clicked
        holder.btnAccept.setOnClickListener(v -> {
            // TODO: Call API to accept request
            handleRequest(user.getId(), "accepted", position);
        });

        // Red Cross Clicked
        holder.btnDecline.setOnClickListener(v -> {
            // TODO: Call API to decline/delete request
            handleRequest(user.getId(), "declined", position);
        });
    }

    private void handleRequest(int userId, String action, int position) {
        // We will fill this with a Retrofit call in the next step
    }

    @Override
    public int getItemCount() { return requestList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView pfp;
        TextView name, time;
        View btnAccept, btnDecline;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pfp = itemView.findViewById(R.id.reqPfp);
            name = itemView.findViewById(R.id.reqName);
            btnAccept = itemView.findViewById(R.id.btnAcceptContainer);
            btnDecline = itemView.findViewById(R.id.btnDeclineContainer);
            time = itemView.findViewById(R.id.reqTime);
        }
    }
}