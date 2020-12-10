package com.example.testnotes.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.JobIntentService;

import com.example.testnotes.services.NotificationService;

public class RebootNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())
                || "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {
            //Log.d("BOOT", "BOOT RECEIVED");
            Intent serviceIntent = new Intent(context, NotificationService.class);
            serviceIntent.putExtra("caller", "RebootNotificationReceiver");
            NotificationService.enqueueWork(context, serviceIntent);
        }

    }
}
