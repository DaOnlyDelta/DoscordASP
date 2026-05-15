package com.example.doscord.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationDeleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int channelId = intent.getIntExtra("channel_id", -1);
        if (channelId != -1) {
            NotificationHelper.clearActiveStyle(context, channelId);
        }
    }
}
