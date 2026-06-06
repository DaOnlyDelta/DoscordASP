package com.example.doscord.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.doscord.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LOADER = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    private static final int VIEW_TYPE_SEQUENTIAL = 2;
    private static final int VIEW_TYPE_SPLITTER = 3;
    private static final int VIEW_TYPE_BEGINNING = 4;

    private List<Message> messagesList;
    private final List<Object> displayList = new ArrayList<>();
    private Context context;
    private User recipient;
    private boolean showLoader = false;
    private boolean isLoaderPlaying = false;

    public MessagesAdapter(List<Message> messagesList, Context context) {
        this.messagesList = messagesList;
        this.context = context;
        updateDisplayList();
    }

    // Method to toggle the loader from CrChatActivity
    public void setShowLoader(boolean showLoader) {
        this.showLoader = showLoader;
        updateDisplayList();
    }

    public void setLoaderPlaying(boolean playing) {
        this.isLoaderPlaying = playing;
        if (showLoader) {
            notifyItemChanged(0);
        }
    }

    public List<Object> getDisplayList() {
        return displayList;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
        updateDisplayList();
    }

    public void updateDisplayList() {
        displayList.clear();
        // Loader or Beginning stays at the absolute top (index 0)
        if (showLoader) {
            displayList.add("LOADER");
        } else {
            displayList.add("BEGINNING");
        }

        String lastDate = "";
        SimpleDateFormat incomingSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displaySdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

        for (Message current : messagesList) {
            String currentDateStr = current.getSentAt().split(" ")[0];
            if (!currentDateStr.equals(lastDate)) {
                try {
                    Date date = incomingSdf.parse(currentDateStr);
                    if (date != null) {
                        displayList.add(displaySdf.format(date));
                    }
                } catch (Exception e) {
                    displayList.add(currentDateStr);
                }
                lastDate = currentDateStr;
            }
            displayList.add(current);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object item = displayList.get(position);

        if (item instanceof String) {
            if (item.equals("LOADER")) return VIEW_TYPE_LOADER;
            if (item.equals("BEGINNING")) return VIEW_TYPE_BEGINNING;
            return VIEW_TYPE_SPLITTER;
        }

        Message current = (Message) item;

        // Find previous message in displayList to check for sequential
        Message previous = null;
        for (int i = position - 1; i >= 0; i--) {
            if (displayList.get(i) instanceof Message) {
                previous = (Message) displayList.get(i);
                break;
            }
        }

        if (previous == null) {
            return VIEW_TYPE_NORMAL;
        }

        // Different sender -> normal message
        if (!current.getSenderId().equals(previous.getSenderId())) {
            return VIEW_TYPE_NORMAL;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date currentDate = sdf.parse(current.getSentAt());
            Date previousDate = sdf.parse(previous.getSentAt());

            if (currentDate != null && previousDate != null) {
                long diffMillis = currentDate.getTime() - previousDate.getTime();
                long diffMinutes = diffMillis / (60 * 1000);

                // Same sender within 5 minute -> sequential
                if (diffMinutes < 5) {
                    return VIEW_TYPE_SEQUENTIAL;
                }
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

        if (viewType == VIEW_TYPE_LOADER) {
            // Reusing your FrameLayout/Lottie idea from earlier
            View view = inflater.inflate(R.layout.item_loader, parent, false);
            return new LoaderViewHolder(view);
        } else if (viewType == VIEW_TYPE_SPLITTER) {
            View view = inflater.inflate(R.layout.item_message_splitter, parent, false);
            return new SplitterViewHolder(view);
        } else if (viewType == VIEW_TYPE_BEGINNING) {
            View view = inflater.inflate(R.layout.item_beginning, parent, false);
            return new BeginningViewHolder(view);
        } else if (viewType == VIEW_TYPE_SEQUENTIAL) {
            View view = inflater.inflate(R.layout.item_seq_message, parent, false);
            return new SequentialViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message, parent, false);
            return new NormalViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = displayList.get(position);

        if (holder instanceof LoaderViewHolder) {
            LoaderViewHolder loaderHolder = (LoaderViewHolder) holder;
            if (loaderHolder.lottie != null) {
                if (isLoaderPlaying) {
                    loaderHolder.lottie.playAnimation();
                } else {
                    loaderHolder.lottie.pauseAnimation();
                    loaderHolder.lottie.setProgress(0f);
                }
            }
        } else if (holder instanceof BeginningViewHolder) {
            bindBeginning((BeginningViewHolder) holder);
        } else if (holder instanceof SplitterViewHolder) {
            ((SplitterViewHolder) holder).dateText.setText((String) item);
        } else if (holder instanceof NormalViewHolder) {
            bindNormalMessage((NormalViewHolder) holder, (Message) item);
        } else if (holder instanceof SequentialViewHolder) {
            bindSequentialMessage((SequentialViewHolder) holder, (Message) item);
        }

        // PADDING LOGIC
        float density = context.getResources().getDisplayMetrics().density;
        int horizontalPadding = (int) (16 * density);
        int topPadding = holder.itemView.getPaddingTop();
        
        // If it's the last item (could be Beginning or Loader if no messages), add bottom padding
        int bottomPadding = (position == getItemCount() - 1) ? (int) (30 * density) : 0;

        if (position != getItemCount() - 1 && holder instanceof SequentialViewHolder) {
            bottomPadding = (int) (2 * density);
        }

        holder.itemView.setPadding(horizontalPadding, topPadding, horizontalPadding, bottomPadding);
    }

    private void bindBeginning(BeginningViewHolder holder) {
        if (recipient == null) return;

        String displayName = recipient.getUsername();
        if (displayName == null || displayName.isEmpty()) {
            displayName = recipient.getDisplayName();
        }
        holder.displayName.setText(displayName);
        holder.username.setText(recipient.getUsername());
        String label = "This is the very beginning of your legendary conversation with " + displayName + ".";
        holder.label.setText(label);

        PfpUtils.loadPfp(context, recipient.getPfp(), holder.pfp);
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

            PfpUtils.loadPfp(context, sender.getPfp(), holder.pfp);
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
    public long getItemId(int position) {
        Object item = displayList.get(position);
        if (item instanceof String) {
            if (item.equals("LOADER")) return -1;
            
            // For splitters, tie the identity to the first message of that day.
            // This ensures that if we load older messages for the same day, 
            // the splitter "moves" with the new top of the day rather than staying 
            // stuck at a fixed position relative to the viewport.
            for (int i = position + 1; i < displayList.size(); i++) {
                Object next = displayList.get(i);
                if (next instanceof Message) {
                    // Combine date hash and message ID to create a unique ID for this 'day-start'
                    return ((long) item.hashCode() << 32) | (((Message) next).getId() & 0xFFFFFFFFL);
                }
                if (next instanceof String) break; 
            }
            return item.hashCode();
        } else if (item instanceof Message) {
            return ((Message) item).getId();
        }
        return RecyclerView.NO_ID;
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    // =========================
    // VIEW HOLDERS
    // =========================

    public static class LoaderViewHolder extends RecyclerView.ViewHolder {
        LottieAnimationView lottie;
        public LoaderViewHolder(@NonNull View itemView) {
            super(itemView);
            lottie = itemView.findViewById(R.id.itemLottie);
        }
    }

    public static class SplitterViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;
        public SplitterViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.itemSplitterDate);
        }
    }

    public static class BeginningViewHolder extends RecyclerView.ViewHolder {
        ImageView pfp;
        TextView displayName, username, label;
        View removeBtn, blockBtn;

        public BeginningViewHolder(@NonNull View itemView) {
            super(itemView);
            pfp = itemView.findViewById(R.id.itemBeginPfp);
            displayName = itemView.findViewById(R.id.itemBeginDisplayName);
            username = itemView.findViewById(R.id.itemBeginUsername);
            label = itemView.findViewById(R.id.itemBeginLabel);
            removeBtn = itemView.findViewById(R.id.itemBeginRemoveBtnContainer);
            blockBtn = itemView.findViewById(R.id.itemBeginBlockBtnContainer);
        }
    }

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