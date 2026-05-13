package com.example.doscord.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.doscord.api.RetrofitClient;
import com.example.doscord.api.TokenLoginResponse;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import retrofit2.Response;

public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SessionManager sessionManager = new SessionManager(context);
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            return Result.success();
        }

        try {
            // Synchronous call to check for updates
            Response<TokenLoginResponse> response = RetrofitClient.getApiService().tokenLogin(token).execute();

            if (response.isSuccessful() && response.body() != null) {
                checkForNewMessages(context, response.body().getUserList(), response.body().getActiveUserId());
            }
        } catch (IOException e) {
            Log.e("NotificationWorker", "Error polling for messages", e);
            return Result.retry();
        }

        return Result.success();
    }

    private void checkForNewMessages(Context context, List<User> users, Integer activeUserId) {
        SharedPreferences prefs = context.getSharedPreferences("notif_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        for (User user : users) {
            // Don't notify for messages sent by the active user
            if (Objects.equals(user.getLastMessageSenderId(), activeUserId)) {
                continue;
            }

            if (user.getLastMessageTime() != null) {
                String key = "last_msg_time_" + user.getChannelId();
                String savedTime = prefs.getString(key, "");

                // If the message is newer than what we last saw
                if (!user.getLastMessageTime().equals(savedTime)) {
                    // Update saved time
                    editor.putString(key, user.getLastMessageTime());
                    
                    // Trigger notification
                    String senderName = user.getNickname();
                    if (senderName == null || senderName.isEmpty()) {
                        senderName = user.getDisplayName();
                    }
                    if (senderName == null || senderName.isEmpty()) {
                        senderName = user.getUsername();
                    }

                    NotificationHelper.showMessageNotification(
                            context,
                            senderName,
                            user.getLastMessage(),
                            user.getPfp(),
                            user.getChannelId()
                    );
                }
            }
        }
        editor.apply();
    }
}
