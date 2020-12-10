package com.example.testnotes.classes;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.testnotes.R;

import static com.example.testnotes.activities.MainActivity.NOTIFICATION_CHANNEL_ID;

public class NotificationPublisher extends BroadcastReceiver {
    public static String NOTIFICATION_ID = "notification-id" ;
    public static String NOTIFICATION = "notification" ;

    public void onReceive (Context context , Intent intent) {
        //Log.d("receive", "recieved");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE) ;
        Notification notification = intent.getParcelableExtra(NOTIFICATION) ;
        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    importance);
            notificationChannel.setDescription(context.getString(R.string.notification_channel_description));
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel) ;
        }
        long id = intent.getLongExtra(NOTIFICATION_ID, 0);
        int int_id = Math.toIntExact(id);
        assert notificationManager != null;
        notificationManager.notify(int_id , notification);
    }
}
