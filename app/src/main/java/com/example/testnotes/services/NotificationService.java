package com.example.testnotes.services;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.example.testnotes.R;
import com.example.testnotes.activities.CreateNoteActivity;
import com.example.testnotes.activities.MainActivity;
import com.example.testnotes.classes.NotificationPublisher;
import com.example.testnotes.classes.Repository;
import com.example.testnotes.entities.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.example.testnotes.activities.MainActivity.NOTIFICATION_CHANNEL_ID;
import static com.example.testnotes.activities.MainActivity.default_notification_channel_id;

public class NotificationService extends JobIntentService {
    public static int JOB_ID = 69420;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, NotificationService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        //Log.d("BOOT", "BOOT RECEIVED WORK GOT");
        String intentType = intent.getExtras().getString("caller");
        if(intentType == null) {
            return;
        }
        if(intentType.equals("RebootNotificationReceiver")) {
            Repository repository = Repository.getInstance(getApplication());
            /*Note log_note = new Note();
            log_note.setId(JOB_ID);
            log_note.setTitle("LOG NOTE");
            log_note.setNoteText("LOG NOTE TEST");
            log_note.setColor("#FFFF0000");
            log_note.setDateTime(
                    new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                            .format(new Date())
            );
            repository.insertNote(log_note);*/
            ArrayList<Note> notes = new ArrayList<>(repository.getAllNotes());
            for (Note note : notes) {
                if(note.getNotificationDateTime() != null) {
                    Notification notification = getNotification(note);

                    Date notificationDate = new Date();
                    SimpleDateFormat smp = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                    try {
                        notificationDate = smp.parse(note.getNotificationDateTime());
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
                    }
                    long delay = notificationDate.getTime();

                    if (new Date().getTime() < delay) {
                        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
                        long notification_id = note.getId();
                        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, notification_id);
                        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                this,
                                0,
                                notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        assert alarmManager != null;
                        alarmManager.set(AlarmManager.RTC_WAKEUP, delay, pendingIntent);
                    }
                }
            }
        }
    }

    private Notification getNotification (Note note) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MainActivity.default_notification_channel_id);
        builder.setContentTitle(note.getTitle());
        builder.setContentText(note.getNoteText());
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(note.getNoteText()));
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setAutoCancel(true);
        builder.setChannelId(MainActivity.NOTIFICATION_CHANNEL_ID);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        Intent resultIntent = new Intent(this, CreateNoteActivity.class);
        resultIntent.putExtra("isViewOrUpdate", true);
        resultIntent.putExtra("note", note);

        SharedPreferences prefs = getSharedPreferences("CommonPrefs",
                Activity.MODE_PRIVATE);
        String language = prefs.getString("Language", "en");
        resultIntent.putExtra("appLanguage", language);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        return builder.build();
    }
}
