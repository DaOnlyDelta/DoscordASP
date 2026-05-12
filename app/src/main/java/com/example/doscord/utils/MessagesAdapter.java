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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_SEQUENTIAL = 1;

    private List<Message> messagesList;
    private Context context;

    public MessagesAdapter(List<Message> messagesList, Context context) {
        this.messagesList = messagesList;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {

        if (position == 0) {
            return VIEW_TYPE_NORMAL;
        }

        Message current = messagesList.get(position);
        Message previous = messagesList.get(position - 1);

        // Different sender -> normal message
        if (current.getSenderId() != previous.getSenderId()) {
            return VIEW_TYPE_NORMAL;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            Date currentDate = sdf.parse(current.getSentAt());
            Date previousDate = sdf.parse(previous.getSentAt());

            if (currentDate == null || previousDate == null) {
                return VIEW_TYPE_NORMAL;
            }

            long diffMillis = currentDate.getTime() - previousDate.getTime();
            long diffMinutes = diffMillis / (60 * 1000);

            // Same sender within 1 minute -> sequential message
            if (diffMinutes < 1) {
                return VIEW_TYPE_SEQUENTIAL;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return VIEW_TYPE_NORMAL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_SEQUENTIAL) {

            View view = inflater.inflate(R.layout.item_seq_message, parent, false);
            return new SequentialViewHolder(view);

        } else {

            View view = inflater.inflate(R.layout.item_message, parent, false);
            return new NormalViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Message message = messagesList.get(position);

        if (holder instanceof NormalViewHolder) {

            bindNormalMessage((NormalViewHolder) holder, message);

        } else if (holder instanceof SequentialViewHolder) {

            bindSequentialMessage((SequentialViewHolder) holder, message);
        }
    }

    private void bindNormalMessage(NormalViewHolder holder, Message message) {

        holder.content.setText(message.getMessageText());

        // Find sender
        User sender = null;

        for (User u : GlobalData.getUserList()) {
            if (u.getId() == message.getSenderId()) {
                sender = u;
                break;
            }
        }

        if (sender != null) {

            String displayName = sender.getNickname();

            if (displayName == null || displayName.isEmpty()) {
                displayName = sender.getDisplayName();
            }

            if (displayName == null || displayName.isEmpty()) {
                displayName = sender.getUsername();
            }

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

        holder.time.setText(formattedTime(message.getSentAt()));
    }

    private void bindSequentialMessage(SequentialViewHolder holder, Message message) {

        holder.content.setText(message.getMessageText());
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    // =========================
    // VIEW HOLDERS
    // =========================

    public static class NormalViewHolder extends RecyclerView.ViewHolder {

        ImageView pfp;
        TextView username, content, time;

        public NormalViewHolder(@NonNull View itemView) {
            super(itemView);

            pfp = itemView.findViewById(R.id.itemMsgPfp);
            username = itemView.findViewById(R.id.itemMsgUsername);
            content = itemView.findViewById(R.id.itemMsgContent);
            time = itemView.findViewById(R.id.itemMsgTime);
        }
    }

    public static class SequentialViewHolder extends RecyclerView.ViewHolder {

        TextView content;

        public SequentialViewHolder(@NonNull View itemView) {
            super(itemView);

            content = itemView.findViewById(R.id.itemMsgContent);
        }
    }

    // =========================
    // TIME FORMATTER
    // =========================

    private String formattedTime(String rawTime) {

        if (rawTime == null || rawTime.isEmpty()) {
            return "";
        }

        try {

            SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            Date messageDate = sdf.parse(rawTime);

            if (messageDate == null) {
                return "";
            }

            Calendar now = Calendar.getInstance();
            Calendar msg = Calendar.getInstance();

            msg.setTime(messageDate);

            SimpleDateFormat timeFormat =
                    new SimpleDateFormat("h:mm a", Locale.getDefault());

            String fTime = timeFormat.format(messageDate);

            // Today
            if (now.get(Calendar.YEAR) == msg.get(Calendar.YEAR)
                    && now.get(Calendar.DAY_OF_YEAR) == msg.get(Calendar.DAY_OF_YEAR)) {

                return fTime;
            }

            // Yesterday
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);

            if (yesterday.get(Calendar.YEAR) == msg.get(Calendar.YEAR)
                    && yesterday.get(Calendar.DAY_OF_YEAR) == msg.get(Calendar.DAY_OF_YEAR)) {

                return "Yesterday at " + fTime;
            }

            // Older
            SimpleDateFormat dateFormat =
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            return dateFormat.format(messageDate) + " " + fTime;

        } catch (Exception e) {

            e.printStackTrace();
            return rawTime;
        }
    }
}