package com.example.doscord.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doscord.R;
import com.example.doscord.api.RetrofitClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {

    private List<User> requestList;
    private Context context;
    private OnRequestHandledListener listener;

    public RequestsAdapter(List<User> requestList, Context context, OnRequestHandledListener listener) {
        this.requestList = requestList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = requestList.get(position);

        String displayName = user.getUsername();
        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            displayName = user.getDisplayName();
        }
        String info = displayName + context.getString(R.string.sent_you_a_friend_request);
        holder.name.setText(info);

        holder.time.setText(Helpers.formatTime(user.getFriendsSince()));

        // Load PFP
        PfpUtils.loadPfp(context, user.getPfp(), holder.pfp);

        // Green Check Clicked
        holder.btnAccept.setOnClickListener(v -> {
            handleRequest(user.getId(), "accepted", position);
        });

        // Red Cross Clicked
        holder.btnDecline.setOnClickListener(v -> {
            handleRequest(user.getId(), "declined", position);
        });
    }

    private void handleRequest(int senderId, String action, int position) {
        int myId = GlobalData.getActiveUserId();

        Map<String, Object> body = new HashMap<>();
        body.put("senderId", senderId);
        body.put("myId", myId);
        body.put("action", action);

        RetrofitClient.getApiService().handleFriendRequest(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    // Success (Status 200)
                    requestList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, requestList.size());
                    if (action.equals("accepted")) {
                        GlobalData.removePending(senderId);

                        if (listener != null) {
                            listener.onRequestProcessed();
                        }
                    }
                } else {
                    // Error (Status 400 or 500)
                    Toast.makeText(context, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(context, "Network failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() { return requestList.size(); }

    public interface OnRequestHandledListener {
        void onRequestProcessed();
    }

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
