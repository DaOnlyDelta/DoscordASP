package com.example.doscord.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.airbnb.lottie.LottieAnimationView;
import com.example.doscord.R;
import com.example.doscord.activities.chatroom.CrNewGroupActivity;
import com.example.doscord.api.BlockFriendRequest;
import com.example.doscord.api.RemoveFriendRequest;
import com.example.doscord.api.RetrofitClient;
import com.example.doscord.models.Channel;
import com.example.doscord.models.User;
import com.example.doscord.utils.GlobalData;
import com.example.doscord.models.Message;
import com.example.doscord.utils.PfpUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface UpdateListener {
        void onUpdateRequired();
    }

    private static final int VIEW_TYPE_LOADER = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    private static final int VIEW_TYPE_SEQUENTIAL = 2;
    private static final int VIEW_TYPE_SPLITTER = 3;
    private static final int VIEW_TYPE_BEGINNING_DM = 4;
    private static final int VIEW_TYPE_BEGINNING_GROUP = 5;
    private static final int VIEW_TYPE_SYSTEM = 6;
    private static final int VIEW_TYPE_VOICE = 7;
    private static final int VIEW_TYPE_VOICE_SEQUENTIAL = 8;

    private List<Message> messagesList;
    private final List<Object> displayList = new ArrayList<>();
    private Context context;
    private UpdateListener updateListener;

    private Channel currentChannel;
    private boolean showLoader = false;
    private boolean isLoaderPlaying = false;

    private MediaPlayer mediaPlayer;
    private String currentlyPlayingUrl = null;
    private int currentlyPlayingPosition = -1;
    private boolean isPrepared = false;
    private Handler progressHandler = new Handler(Looper.getMainLooper());
    private Runnable progressRunnable;
    private final Map<String, String> durationCache = new HashMap<>();
    private final Map<String, Integer> positionCache = new HashMap<>();

    public MessagesAdapter(List<Message> messagesList, Context context) {
        this.messagesList = messagesList;
        this.context = context;
        updateDisplayList();
    }

    public void setUpdateListener(UpdateListener updateListener) {
        this.updateListener = updateListener;
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

    @SuppressLint("NotifyDataSetChanged")
    public void setChannelContext(Channel channel) {
        this.currentChannel = channel;
        updateDisplayList();
        notifyDataSetChanged();
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
        preloadVoiceDurations();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = displayList.get(position);

        if (item instanceof String) {
            if (item.equals("LOADER")) return VIEW_TYPE_LOADER;
            if (item.equals("BEGINNING")) {
                if (currentChannel != null && currentChannel.isGroup()) {
                    return VIEW_TYPE_BEGINNING_GROUP;
                }
                return VIEW_TYPE_BEGINNING_DM;
            }
            return VIEW_TYPE_SPLITTER;
        }

        Message current = (Message) item;
        boolean isVoice = "voice".equalsIgnoreCase(current.getType());

        if ("system".equalsIgnoreCase(current.getType())) {
            return VIEW_TYPE_SYSTEM;
        }

        Message previous = null;
        if (position > 0) {
            Object prevItem = displayList.get(position - 1);
            if (prevItem instanceof Message) {
                Message temp = (Message) prevItem;
                if (!"system".equalsIgnoreCase(temp.getType())) {
                    previous = temp;
                }
            }
        }

        if (previous == null) return isVoice ? VIEW_TYPE_VOICE : VIEW_TYPE_NORMAL;
        if (current.getSenderId() == null || previous.getSenderId() == null) return isVoice ? VIEW_TYPE_VOICE : VIEW_TYPE_NORMAL;
        if (!current.getSenderId().equals(previous.getSenderId())) return isVoice ? VIEW_TYPE_VOICE : VIEW_TYPE_NORMAL;

        String t1 = current.getSentAt();
        String t2 = previous.getSentAt();

        if (t1 == null || t2 == null) return isVoice ? VIEW_TYPE_VOICE : VIEW_TYPE_NORMAL;

        try {
            // Flexible parsing to handle variations (space vs T separator)
            String normalizedT1 = t1.replace('T', ' ');
            String normalizedT2 = t2.replace('T', ' ');

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date currentDate = sdf.parse(normalizedT1.substring(0, Math.min(normalizedT1.length(), 19)));
            Date previousDate = sdf.parse(normalizedT2.substring(0, Math.min(normalizedT2.length(), 19)));

            if (currentDate != null && previousDate != null) {
                long diffMillis = Math.abs(currentDate.getTime() - previousDate.getTime());
                long diffMinutes = diffMillis / (60 * 1000);

                if (diffMinutes < 5) {
                    return isVoice ? VIEW_TYPE_VOICE_SEQUENTIAL : VIEW_TYPE_SEQUENTIAL;
                }
            }
        } catch (Exception e) {
            // Fallback: if parsing fails but they are extremely close (same string), group them
            if (t1.equals(t2)) return isVoice ? VIEW_TYPE_VOICE_SEQUENTIAL : VIEW_TYPE_SEQUENTIAL;
        }

        return isVoice ? VIEW_TYPE_VOICE : VIEW_TYPE_NORMAL;
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
        } else if (viewType == VIEW_TYPE_BEGINNING_GROUP) {
            View view = inflater.inflate(R.layout.item_group_beginning, parent, false);
            return new BeginningViewHolder(view, true);
        } else if (viewType == VIEW_TYPE_BEGINNING_DM) {
            View view = inflater.inflate(R.layout.item_beginning, parent, false);
            return new BeginningViewHolder(view, false);
        } else if (viewType == VIEW_TYPE_SYSTEM) {
            View view = inflater.inflate(R.layout.item_system_message, parent, false);
            return new SystemViewHolder(view);
        } else if (viewType == VIEW_TYPE_SEQUENTIAL) {
            View view = inflater.inflate(R.layout.item_seq_message, parent, false);
            return new SequentialViewHolder(view);
        } else if (viewType == VIEW_TYPE_VOICE) {
            View view = inflater.inflate(R.layout.item_voice_message, parent, false);
            return new VoiceViewHolder(view);
        } else if (viewType == VIEW_TYPE_VOICE_SEQUENTIAL) {
            View view = inflater.inflate(R.layout.item_voice_message_seq, parent, false);
            return new VoiceSequentialViewHolder(view);
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
            bindNormalMessage((NormalViewHolder) holder, (Message) item, position);
        } else if (holder instanceof VoiceViewHolder) {
            bindVoiceMessage((VoiceViewHolder) holder, (Message) item, position);
        } else if (holder instanceof VoiceSequentialViewHolder) {
            bindVoiceSequentialMessage((VoiceSequentialViewHolder) holder, (Message) item, position);
        } else if (holder instanceof SystemViewHolder) {
            SystemViewHolder sysHolder = (SystemViewHolder) holder;
            Message msg = (Message) item;

            sysHolder.text.setText(msg.getMessageText());
            sysHolder.date.setText(formattedTime(msg.getSentAt()));

            String messageText = msg.getMessageText();
            if (messageText != null) {
                if (messageText.contains("left")) {
                    // Red action: User left
                    sysHolder.arrow.setRotationY(0f);
                    sysHolder.arrow.setImageResource(R.drawable.back_arrow);
                    sysHolder.arrow.setImageTintList(android.content.res.ColorStateList.valueOf(
                            context.getColor(R.color.red)
                    ));
                } else if (messageText.contains("group name")) {
                    // White action: Group name changed or reset
                    sysHolder.arrow.setRotationY(0f);
                    sysHolder.arrow.setImageResource(R.drawable.pencil);
                    sysHolder.arrow.setImageTintList(android.content.res.ColorStateList.valueOf(
                            context.getColor(R.color.white)
                    ));
                } else {
                    // Green action: User joined or other positive log
                    sysHolder.arrow.setRotationY(180f);
                    sysHolder.arrow.setImageResource(R.drawable.back_arrow);
                    sysHolder.arrow.setImageTintList(android.content.res.ColorStateList.valueOf(
                            context.getColor(R.color.green)
                    ));
                }
            }
        } else if (holder instanceof SequentialViewHolder) {
            SequentialViewHolder seqHolder = (SequentialViewHolder) holder;
            Message msg = (Message) item;
            if (msg.isEdited()) {
                String fullText = msg.getMessageText() + " (Edited)";
                SpannableString spannable = new SpannableString(fullText);
                int start = msg.getMessageText().length();
                int end = fullText.length();
                spannable.setSpan(new ForegroundColorSpan(context.getColor(R.color.gray)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new RelativeSizeSpan(0.8f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                seqHolder.content.setText(spannable);
            } else {
                seqHolder.content.setText(msg.getMessageText());
            }

            if (msg.getMessageText() == null || msg.getMessageText().trim().isEmpty()) {
                seqHolder.content.setVisibility(View.GONE);
            } else {
                seqHolder.content.setVisibility(View.VISIBLE);
            }

            if (msg.getMediaUrl() != null && !msg.getMediaUrl().isEmpty()) {
                seqHolder.imageContainer.setVisibility(View.VISIBLE);
                String fullUrl = "https://doscord.top/api/uploads/" + msg.getMediaUrl();
                Glide.with(context)
                        .load(fullUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(seqHolder.image);
            } else {
                seqHolder.imageContainer.setVisibility(View.GONE);
            }
        }

        // Apply message bubbles padding properties, including system alerts if at the end
        float density = context.getResources().getDisplayMetrics().density;
        int horizontalPadding = (int) (16 * density);
        int topPadding = holder.itemView.getPaddingTop();
        int bottomPadding = (position == getItemCount() - 1) ? (int) (30 * density) : 0;

        if (position != getItemCount() - 1 && holder instanceof SequentialViewHolder) {
            bottomPadding = (int) (2 * density);
        }

        // SystemViewHolder must also have padding applied to reset dynamic changes if it's no longer the last item
        if (holder instanceof SystemViewHolder) {
            holder.itemView.setPadding(holder.itemView.getPaddingLeft(), topPadding, holder.itemView.getPaddingRight(), bottomPadding);
        } else {
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
            String sTitle = "This is the very beginning of your legendary conversation with " + dName + ".";
            holder.label.setText(sTitle);

            PfpUtils.loadPfp(context, currentChannel.getDmRecipientPfp(), holder.pfp);

            // Set up click listeners for your new group action buttons
            if (holder.removeBtn != null) {
                holder.removeBtn.setOnClickListener(v -> {
                    String name = currentChannel.getDmDisplayNameOrNickname();
                    new MaterialAlertDialogBuilder(v.getContext())
                            .setTitle("Remove Friend")
                            .setMessage("Are you sure you want to remove " + name + " from your friends?")
                            .setPositiveButton("Remove", (dialog, which) -> {
                                if (currentChannel != null && currentChannel.getDmRecipientId() != null) {
                                    int activeUserId = GlobalData.getActiveUserId();
                                    int friendId = currentChannel.getDmRecipientId();

                                    RemoveFriendRequest req = new RemoveFriendRequest(activeUserId, friendId);
                                    Call<Void> call = RetrofitClient.getApiService().removeFriend(req);
                                    call.enqueue(new Callback<Void>() {
                                        @Override
                                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                            if (response.isSuccessful()) {
                                                Activity activity = getActivity(v.getContext());
                                                if (activity != null) activity.finish();
                                            }
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
                                    });
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });
            }
            if (holder.blockBtn != null) {
                holder.blockBtn.setOnClickListener(v -> {
                    String name = currentChannel.getDmDisplayNameOrNickname();
                    new MaterialAlertDialogBuilder(v.getContext())
                            .setTitle("Block User")
                            .setMessage("Are you sure you want to block " + name + "? They will no longer be able to add you.")
                            .setPositiveButton("Block", (dialog, which) -> {
                                if (currentChannel != null && currentChannel.getDmRecipientId() != null) {
                                    int activeUserId = GlobalData.getActiveUserId();
                                    int friendId = currentChannel.getDmRecipientId();

                                    BlockFriendRequest req = new BlockFriendRequest(activeUserId, friendId);
                                    Call<Void> call = RetrofitClient.getApiService().blockFriend(req);
                                    call.enqueue(new Callback<Void>() {
                                        @Override
                                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                            if (response.isSuccessful()) {
                                                Activity activity = getActivity(v.getContext());
                                                if (activity != null) activity.finish();
                                            }
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
                                    });
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });
            }
        }
    }

    private void bindNormalMessage(NormalViewHolder holder, Message message, int position) {
        if (message.isEdited()) {
            String fullText = message.getMessageText() + " (Edited)";
            SpannableString spannable = new SpannableString(fullText);
            int start = message.getMessageText().length();
            int end = fullText.length();
            spannable.setSpan(new ForegroundColorSpan(context.getColor(R.color.gray)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new RelativeSizeSpan(0.8f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.content.setText(spannable);
        } else {
            holder.content.setText(message.getMessageText());
        }

        if (message.getMessageText() == null || message.getMessageText().trim().isEmpty()) {
            holder.content.setVisibility(View.GONE);
        } else {
            holder.content.setVisibility(View.VISIBLE);
        }

        if (message.getMediaUrl() != null && !message.getMediaUrl().isEmpty()) {
            holder.imageContainer.setVisibility(View.VISIBLE);
            String fullUrl = "https://doscord.top/api/uploads/" + message.getMediaUrl();
            Glide.with(context)
                    .load(fullUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.image);
        } else {
            holder.imageContainer.setVisibility(View.GONE);
        }

        holder.time.setText(formattedTime(message.getSentAt()));

        // Dynamically resolve the user model from our local GlobalData caches
        User sender = GlobalData.findUserById(message.getSenderId());

        if (sender != null) {
            holder.username.setText(sender.getNameToDisplay());
            PfpUtils.loadPfp(context, sender.getPfp(), holder.pfp);
        } else {
            // Fallback layout state if the user profile isn't cached anywhere locally yet
            holder.username.setText("User #" + message.getSenderId());
            holder.pfp.setImageResource(R.drawable.icon);
        }
    }

    private void bindVoiceMessage(VoiceViewHolder holder, Message message, int position) {
        String url = message.getMediaUrl();
        if (url != null && url.equals(currentlyPlayingUrl)) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                holder.playBtn.setImageResource(R.drawable.pause);
            } else {
                holder.playBtn.setImageResource(R.drawable.play);
            }
            if (isPrepared) {
                updateProgressUI(holder.seekBar, holder.timer, url);
            }
        } else {
            holder.playBtn.setImageResource(R.drawable.play);
            updateProgressUI(holder.seekBar, holder.timer, url);
        }

        holder.playBtn.setOnClickListener(v -> playVoiceMessage(message.getMediaUrl(), position));
        holder.time.setText(formattedTime(message.getSentAt()));

        User sender = GlobalData.findUserById(message.getSenderId());
        if (sender != null) {
            holder.username.setText(sender.getNameToDisplay());
            PfpUtils.loadPfp(context, sender.getPfp(), holder.pfp);
        } else {
            holder.username.setText("User #" + message.getSenderId());
            holder.pfp.setImageResource(R.drawable.icon);
        }
    }

    private void bindVoiceSequentialMessage(VoiceSequentialViewHolder holder, Message message, int position) {
        String url = message.getMediaUrl();
        if (url != null && url.equals(currentlyPlayingUrl)) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                holder.playBtn.setImageResource(R.drawable.pause);
            } else {
                holder.playBtn.setImageResource(R.drawable.play);
            }
            if (isPrepared) {
                updateProgressUI(holder.seekBar, holder.timer, url);
            }
        } else {
            holder.playBtn.setImageResource(R.drawable.play);
            updateProgressUI(holder.seekBar, holder.timer, url);
        }

        holder.playBtn.setOnClickListener(v -> playVoiceMessage(message.getMediaUrl(), position));
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
        ImageView pfp, image;
        TextView username, content, time;
        View imageContainer;

        public NormalViewHolder(@NonNull View itemView) {
            super(itemView);
            pfp = itemView.findViewById(R.id.itemMsgPfp);
            username = itemView.findViewById(R.id.itemMsgUsername);
            content = itemView.findViewById(R.id.itemMsgContent);
            time = itemView.findViewById(R.id.itemMsgTime);
            image = itemView.findViewById(R.id.itemMsgImage);
            imageContainer = itemView.findViewById(R.id.itemMsgImageContainer);
        }
    }

    public static class SequentialViewHolder extends RecyclerView.ViewHolder {
        TextView content;
        ImageView image;
        View imageContainer;

        public SequentialViewHolder(@NonNull View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.itemMsgContent);
            image = itemView.findViewById(R.id.itemMsgImage);
            imageContainer = itemView.findViewById(R.id.itemMsgImageContainer);
        }
    }

    public static class VoiceViewHolder extends RecyclerView.ViewHolder {
        ImageView pfp, playBtn;
        ProgressBar seekBar;
        TextView username, time, timer;

        public VoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            pfp = itemView.findViewById(R.id.itemMsgPfp);
            playBtn = itemView.findViewById(R.id.itemMsgPlayBtn);
            seekBar = itemView.findViewById(R.id.voiceSeekBar);
            timer = itemView.findViewById(R.id.voiceTimer);
            username = itemView.findViewById(R.id.itemMsgUsername);
            time = itemView.findViewById(R.id.itemMsgTime);
        }
    }

    public static class VoiceSequentialViewHolder extends RecyclerView.ViewHolder {
        ImageView playBtn;
        ProgressBar seekBar;
        TextView timer;

        public VoiceSequentialViewHolder(@NonNull View itemView) {
            super(itemView);
            playBtn = itemView.findViewById(R.id.itemMsgPlayBtn);
            seekBar = itemView.findViewById(R.id.voiceSeekBar);
            timer = itemView.findViewById(R.id.voiceTimer);
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

    private void playVoiceMessage(String urlPart, int position) {
        if (urlPart == null || urlPart.isEmpty()) return;

        if (mediaPlayer != null && urlPart.equals(currentlyPlayingUrl)) {
            if (isPrepared) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    positionCache.put(urlPart, mediaPlayer.getCurrentPosition());
                    stopProgressUpdate();
                } else {
                    mediaPlayer.start();
                    startProgressUpdate();
                }
                notifyItemChanged(position);
            }
            return;
        }

        int previousPosition = currentlyPlayingPosition;
        if (mediaPlayer != null) {
            positionCache.put(currentlyPlayingUrl, mediaPlayer.getCurrentPosition());
        }
        stopAndReleasePlayer();

        currentlyPlayingUrl = urlPart;
        currentlyPlayingPosition = position;
        
        if (previousPosition != -1) notifyItemChanged(previousPosition);
        notifyItemChanged(position);

        String fullUrl = getVoiceFullUrl(urlPart);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());

        try {
            mediaPlayer.setDataSource(fullUrl);
            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                Integer savedPos = positionCache.get(urlPart);
                if (savedPos != null) {
                    mp.seekTo(savedPos);
                }
                mp.start();
                startProgressUpdate();
                if (currentlyPlayingPosition != -1) notifyItemChanged(currentlyPlayingPosition);
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                int pos = currentlyPlayingPosition;
                positionCache.put(urlPart, 0); // Reset for this message
                stopAndReleasePlayer();
                if (pos != -1) notifyItemChanged(pos);
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(context, "Playback error: " + what, Toast.LENGTH_SHORT).show();
                int pos = currentlyPlayingPosition;
                stopAndReleasePlayer();
                if (pos != -1) notifyItemChanged(pos);
                return true;
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to load audio", Toast.LENGTH_SHORT).show();
            stopAndReleasePlayer();
            notifyItemChanged(position);
        }
    }

    private String getVoiceFullUrl(String urlPart) {
        if (urlPart == null || urlPart.isEmpty()) return "";
        if (urlPart.startsWith("http")) return urlPart;
        
        if (urlPart.startsWith("uploads/")) {
            return "https://doscord.top/api/" + urlPart;
        } else if (urlPart.startsWith("attachments/")) {
            return "https://doscord.top/api/uploads/" + urlPart;
        } else {
            return "https://doscord.top/api/uploads/attachments/" + urlPart;
        }
    }

    private void preloadVoiceDurations() {
        for (Message m : messagesList) {
            if ("voice".equalsIgnoreCase(m.getType())) {
                String url = m.getMediaUrl();
                if (url != null && !durationCache.containsKey(url)) {
                    new Thread(() -> {
                        try {
                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            retriever.setDataSource(getVoiceFullUrl(url), new HashMap<>());
                            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            long durationMs = Long.parseLong(time);
                            String formatted = formatDuration((int) durationMs);
                            retriever.release();
                            
                            new Handler(Looper.getMainLooper()).post(() -> {
                                durationCache.put(url, formatted);
                                // Find all instances of this message in displayList and notify
                                for (int i = 0; i < displayList.size(); i++) {
                                    Object obj = displayList.get(i);
                                    if (obj instanceof Message && url.equals(((Message) obj).getMediaUrl())) {
                                        notifyItemChanged(i);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
        }
    }

    private void stopAndReleasePlayer() {
        isPrepared = false;
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (Exception ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
        currentlyPlayingUrl = null;
        currentlyPlayingPosition = -1;
        stopProgressUpdate();
    }

    private void startProgressUpdate() {
        stopProgressUpdate();
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    if (currentlyPlayingPosition != -1) {
                        notifyItemChanged(currentlyPlayingPosition, "PAYLOAD_PROGRESS");
                    }
                    progressHandler.postDelayed(this, 500);
                }
            }
        };
        progressHandler.postDelayed(progressRunnable, 0);
    }

    private void stopProgressUpdate() {
        if (progressRunnable != null) {
            progressHandler.removeCallbacks(progressRunnable);
            progressRunnable = null;
        }
    }

    private void updateProgressUI(ProgressBar seekBar, TextView timer, String url) {
        if (mediaPlayer != null && currentlyPlayingUrl != null && isPrepared && url.equals(currentlyPlayingUrl)) {
            try {
                int current = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();
                if (duration > 0) {
                    seekBar.setMax(duration);
                    seekBar.setProgress(current);
                    timer.setText(formatDuration(current) + " / " + formatDuration(duration));
                }
            } catch (IllegalStateException e) {
                // Player was likely released or not in the correct state
            }
        } else {
            // Check if we have a saved position to display
            Integer savedPos = positionCache.get(url);
            String total = durationCache.get(url);
            if (savedPos != null && savedPos > 0) {
                seekBar.setProgress(savedPos);
                timer.setText(formatDuration(savedPos) + " / " + (total != null ? total : "0:00"));
            } else {
                seekBar.setProgress(0);
                timer.setText("0:00 / " + (total != null ? total : "0:00"));
            }
        }
    }

    private String formatDuration(int millis) {
        int seconds = (millis / 1000) % 60;
        int minutes = (millis / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.contains("PAYLOAD_PROGRESS")) {
            if (holder instanceof VoiceViewHolder) {
                VoiceViewHolder vh = (VoiceViewHolder) holder;
                Message m = (Message) displayList.get(position);
                updateProgressUI(vh.seekBar, vh.timer, m.getMediaUrl());
            } else if (holder instanceof VoiceSequentialViewHolder) {
                VoiceSequentialViewHolder vh = (VoiceSequentialViewHolder) holder;
                Message m = (Message) displayList.get(position);
                updateProgressUI(vh.seekBar, vh.timer, m.getMediaUrl());
            }
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    public void release() {
        stopAndReleasePlayer();
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

    private Activity getActivity(Context context) {
        if (context instanceof Activity) return (Activity) context;
        if (context instanceof ContextWrapper) return getActivity(((ContextWrapper) context).getBaseContext());
        return null;
    }
}
