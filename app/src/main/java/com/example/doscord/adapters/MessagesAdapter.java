package com.example.doscord.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.doscord.R;
import com.example.doscord.activities.chatroom.CrMainActivity;
import com.example.doscord.activities.chatroom.CrNewGroupActivity;
import com.example.doscord.models.Channel;
import com.example.doscord.models.User;
import com.example.doscord.utils.GlobalData;
import com.example.doscord.models.Message;
import com.example.doscord.utils.PfpUtils;

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
    private static final int VIEW_TYPE_SYSTEM = 5;

    private List<Message> messagesList;
    private final List<Object> displayList = new ArrayList<>();
    private Context context;

    private Channel currentChannel;
    private boolean showLoader = false;
    private boolean isLoaderPlaying = false;

    public MessagesAdapter(List<Message> messagesList, Context context) {
        this.messagesList = messagesList;
        this.context = context;
        updateDisplayList();
    }

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

    public void setChannelContext(Channel channel) {
        this.currentChannel = channel;
        updateDisplayList();
    }

    public void updateDisplayList() {
        displayList.clear();
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

        if ("system".equalsIgnoreCase(current.getType())) {
            return VIEW_TYPE_SYSTEM;
        }

        Message previous = null;
        for (int i = position - 1; i >= 0; i--) {
            // Ensure sequential processing skips over system row objects gracefully
            if (displayList.get(i) instanceof Message) {
                Message temp = (Message) displayList.get(i);
                if (!"system".equalsIgnoreCase(temp.getType())) {
                    previous = temp;
                    break;
                }
            }
        }

        if (previous == null) return VIEW_TYPE_NORMAL;
        if (!current.getSenderId().equals(previous.getSenderId())) return VIEW_TYPE_NORMAL;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date currentDate = sdf.parse(current.getSentAt());
            Date previousDate = sdf.parse(previous.getSentAt());

            if (currentDate != null && previousDate != null) {
                long diffMillis = currentDate.getTime() - previousDate.getTime();
                long diffMinutes = diffMillis / (60 * 1000);

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
            View view = inflater.inflate(R.layout.item_loader, parent, false);
            return new LoaderViewHolder(view);
        } else if (viewType == VIEW_TYPE_SPLITTER) {
            View view = inflater.inflate(R.layout.item_message_splitter, parent, false);
            return new SplitterViewHolder(view);
        } else if (viewType == VIEW_TYPE_BEGINNING) {
            // Dynamically select the layout file based on group status
            if (currentChannel != null && currentChannel.isGroup()) {
                View view = inflater.inflate(R.layout.item_group_beginning, parent, false);
                return new BeginningViewHolder(view, true);
            } else {
                View view = inflater.inflate(R.layout.item_beginning, parent, false);
                return new BeginningViewHolder(view, false);
            }
        } else if (viewType == VIEW_TYPE_SYSTEM) {
            View view = inflater.inflate(R.layout.item_system_message, parent, false);
            return new SystemViewHolder(view);
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
        } else if (holder instanceof SystemViewHolder) {
            SystemViewHolder sysHolder = (SystemViewHolder) holder;
            Message msg = (Message) item;

            sysHolder.text.setText(msg.getMessageText());
            sysHolder.date.setText(formattedTime(msg.getSentAt()));

            // Check for red action
            if (msg.getMessageText().split(" ")[1].equals("removed")) {
                sysHolder.arrow.setRotationY(0f);
                // Fetch the color from your resources and apply it as a tint list
                sysHolder.arrow.setImageTintList(android.content.res.ColorStateList.valueOf(
                        context.getColor(R.color.red)
                ));
            }
        } else if (holder instanceof SequentialViewHolder) {
            ((SequentialViewHolder) holder).content.setText(((Message) item).getMessageText());
        }

        // Only modify message bubbles padding properties, leave system alerts clean
        if (!(holder instanceof SystemViewHolder)) {
            float density = context.getResources().getDisplayMetrics().density;
            int horizontalPadding = (int) (16 * density);
            int topPadding = holder.itemView.getPaddingTop();
            int bottomPadding = (position == getItemCount() - 1) ? (int) (30 * density) : 0;

            if (position != getItemCount() - 1 && holder instanceof SequentialViewHolder) {
                bottomPadding = (int) (2 * density);
            }

            holder.itemView.setPadding(horizontalPadding, topPadding, horizontalPadding, bottomPadding);
        }
    }

    private void bindBeginning(BeginningViewHolder holder) {
        if (currentChannel == null) return;

        if (currentChannel.isGroup()) {
            String rawGroupName = currentChannel.getGroupName();
            String gName;

            if (rawGroupName == null || rawGroupName.trim().isEmpty() || rawGroupName.equalsIgnoreCase("null")) {
                gName = "Empty Group";
            } else {
                String[] parts = rawGroupName.split(", ");
                if (parts.length > 3) {
                    gName = parts[0] + ", " + parts[1] + ", " + parts[2] + "...";
                } else {
                    gName = rawGroupName;
                }
            }

            // Binding specifically to item_group_beginning fields
            holder.displayName.setText(gName);
            PfpUtils.loadGroupPfp(context, currentChannel.getGroupPfp(), holder.pfp);

            // Set up click listeners for your new group action buttons
            if (holder.groupInviteBtn != null) {
                holder.groupInviteBtn.setOnClickListener(v -> {
                    Intent intent = new Intent(context, CrNewGroupActivity.class);
                    intent.putExtra("isAddingMembers", true);
                    intent.putExtra("channelId", currentChannel.getChannelId());
                    context.startActivity(intent);
                });
            }
        } else {
            // Binding standard 1-to-1 Direct Messages
            String dName = currentChannel.getDmDisplayNameOrNickname();
            if (dName == null || dName.isEmpty()) {
                dName = currentChannel.getDmRecipientUsername();
            }
            holder.displayName.setText(dName);
            if (holder.username != null) {
                holder.username.setVisibility(View.VISIBLE);
                holder.username.setText("@" + currentChannel.getDmRecipientUsername());
            }
            holder.label.setText("This is the very beginning of your legendary conversation with " + dName + ".");

            if (holder.removeBtn != null) holder.removeBtn.setVisibility(View.VISIBLE);
            if (holder.blockBtn != null) holder.blockBtn.setVisibility(View.VISIBLE);

            PfpUtils.loadPfp(context, currentChannel.getDmRecipientPfp(), holder.pfp);
        }
    }

    private void bindNormalMessage(NormalViewHolder holder, Message message) {
        holder.content.setText(message.getMessageText());
        holder.time.setText(formattedTime(message.getSentAt()));

        // Dynamically resolve the user model from our local GlobalData caches
        User sender = GlobalData.findUserById(message.getSenderId());

        if (sender != null) {
            String nameToDisplay = sender.getDisplayName() != null && !sender.getDisplayName().isEmpty()
                    ? sender.getDisplayName()
                    : sender.getUsername();

            holder.username.setText(nameToDisplay);
            PfpUtils.loadPfp(context, sender.getPfp(), holder.pfp);
        } else {
            // Fallback layout state if the user profile isn't cached anywhere locally yet
            holder.username.setText("User #" + message.getSenderId());
            holder.pfp.setImageResource(R.drawable.icon);
        }
    }

    @Override
    public long getItemId(int position) {
        Object item = displayList.get(position);
        if (item instanceof String) {
            if (item.equals("LOADER")) return -1;
            for (int i = position + 1; i < displayList.size(); i++) {
                Object next = displayList.get(i);
                if (next instanceof Message) {
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
        boolean isGroupForm;
        ImageView pfp;
        TextView displayName, username, label;

        // DM Layout Specifics
        View removeBtn, blockBtn;

        // Group Layout Specifics
        TextView groupBeginLabel;
        View groupInviteBtn, groupEditBtn;

        public BeginningViewHolder(@NonNull View itemView, boolean isGroupForm) {
            super(itemView);
            this.isGroupForm = isGroupForm;

            if (isGroupForm) {
                pfp = itemView.findViewById(R.id.itemGroupBeginPfp);
                displayName = itemView.findViewById(R.id.itemGroupBeginDisplayName);
                groupBeginLabel = itemView.findViewById(R.id.itemGroupBeginLabel);
                groupInviteBtn = itemView.findViewById(R.id.itemGroupBeginInviteBtnContainer);
            } else {
                pfp = itemView.findViewById(R.id.itemBeginPfp);
                displayName = itemView.findViewById(R.id.itemBeginDisplayName);
                username = itemView.findViewById(R.id.itemBeginUsername);
                label = itemView.findViewById(R.id.itemBeginLabel);
                removeBtn = itemView.findViewById(R.id.itemBeginRemoveBtnContainer);
                blockBtn = itemView.findViewById(R.id.itemBeginBlockBtnContainer);
            }
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

    public static class SystemViewHolder extends RecyclerView.ViewHolder {
        ImageView arrow;
        TextView text, date;

        public SystemViewHolder(@NonNull View itemView) {
            super(itemView);
            arrow = itemView.findViewById(R.id.itemSystemArrow);
            text = itemView.findViewById(R.id.itemSystemText);
            date = itemView.findViewById(R.id.itemSystemDate);
        }
    }

    private String formattedTime(String rawTime) {
        if (rawTime == null || rawTime.isEmpty()) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date messageDate = sdf.parse(rawTime);
            if (messageDate == null) return "";

            Calendar now = Calendar.getInstance();
            Calendar msg = Calendar.getInstance();
            msg.setTime(messageDate);

            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            String fTime = timeFormat.format(messageDate);

            if (now.get(Calendar.YEAR) == msg.get(Calendar.YEAR)
                    && now.get(Calendar.DAY_OF_YEAR) == msg.get(Calendar.DAY_OF_YEAR)) {
                return fTime;
            }

            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);
            if (yesterday.get(Calendar.YEAR) == msg.get(Calendar.YEAR)
                    && yesterday.get(Calendar.DAY_OF_YEAR) == msg.get(Calendar.DAY_OF_YEAR)) {
                return "Yesterday at " + fTime;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return dateFormat.format(messageDate) + " " + fTime;
        } catch (Exception e) {
            e.printStackTrace();
            return rawTime;
        }
    }
}