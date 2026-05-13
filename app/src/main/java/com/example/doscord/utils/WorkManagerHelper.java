package com.example.doscord.utils;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class WorkManagerHelper {

    public static void startMessagePolling(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // 15 minutes is the minimum interval for PeriodicWorkRequest
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                NotificationWorker.class,
                15, TimeUnit.MINUTES
        )
                .setConstraints(constraints)
                .addTag("MessagePolling")
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "MessagePolling",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
    }

    public static void stopMessagePolling(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("MessagePolling");
    }
}
