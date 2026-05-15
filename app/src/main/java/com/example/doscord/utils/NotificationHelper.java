package com.example.doscord.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.doscord.R;
import com.example.doscord.activities.chatroom.CrChatActivity;
import com.example.doscord.activities.chatroom.CrMainActivity;

import java.util.HashMap;
import java.util.Map;

public class NotificationHelper {

    private static final String CHANNEL_ID = "doscord_messages";
    private static final String CHANNEL_NAME = "Messages";
    private static final String CHANNEL_DESC = "Notifications for new messages";

    // Store active messaging styles to append messages
    private static final Map<Integer, NotificationCompat.MessagingStyle> activeStyles = new HashMap<>();

    public static void showMessageNotification(Context context, String username, String message, String pfpPath, int channelId) {
        // If we are currently chatting with this person, don't show a notification
        if (CrChatActivity.activeChannelId == channelId) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create Channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);

            // Set custom sound if it exists in res/raw/notif_sound.mp3
            Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notif_sound);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);

            notificationManager.createNotificationChannel(channel);
        }

        // Intent to open the chat when clicked
        Intent intent = new Intent(context, CrChatActivity.class);
        intent.putExtra("channel_id", channelId);
        intent.putExtra("chat_name", username);
        
        // TaskStackBuilder ensures the back stack is maintained
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);
        
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                channelId,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent for when the user swipes away the notification
        Intent deleteIntent = new Intent(context, NotificationDeleteReceiver.class);
        deleteIntent.putExtra("channel_id", channelId);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(
                context,
                channelId,
                deleteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notif_sound);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .setOnlyAlertOnce(true);
// Only play sound/popup for the first message in a bundle

        if (pfpPath != null && !pfpPath.isEmpty()) {
            String fullPath = "https://doscord.top/api/images/" + pfpPath;
            Glide.with(context.getApplicationContext())
                    .asBitmap()
                    .load(fullPath)
                    .circleCrop()
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            // Set LargeIcon to keep it on the right side
                            builder.setLargeIcon(resource);
                            
                            Person userPerson = new Person.Builder()
                                    .setName(username)
                                    // No icon here to avoid moving it to the left in MessagingStyle
                                    .build();

                            NotificationCompat.MessagingStyle style = activeStyles.get(channelId);
                            if (style == null) {
                                style = new NotificationCompat.MessagingStyle(userPerson);
                            }
                            
                            style.addMessage(message, System.currentTimeMillis(), userPerson);
                            activeStyles.put(channelId, style);

                            builder.setStyle(style);
                            notificationManager.notify(channelId, builder.build());
                        }

                        @Override
                        public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) {}

                        @Override
                        public void onLoadFailed(@Nullable android.graphics.drawable.Drawable errorDrawable) {
                            NotificationCompat.MessagingStyle style = activeStyles.get(channelId);
                            if (style == null) {
                                builder.setContentTitle(username).setContentText(message);
                            } else {
                                Person p = new Person.Builder().setName(username).build();
                                style.addMessage(message, System.currentTimeMillis(), p);
                                builder.setStyle(style);
                            }
                            notificationManager.notify(channelId, builder.build());
                        }
                    });
        } else {
            builder.setContentTitle(username).setContentText(message);
            notificationManager.notify(channelId, builder.build());
        }
    }
    
    // Call this when the user opens a chat or clears notifications
    public static void clearActiveStyle(Context context, int channelId) {
        activeStyles.remove(channelId);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(channelId);
    }
}
