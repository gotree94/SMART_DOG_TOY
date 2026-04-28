package com.jieli.healthaide.ui.sports.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.ContentActivity;

@Deprecated
public class NotificationService extends Service {


    private final static int NOTIFY_ID = 1;


    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NOTIFY_ID, createNotify(getApplication()));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    private Notification createNotify(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        String id = "JieLi_Running_Service";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String name = getString(R.string.movement_record);
            NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_MIN);
            channel.setSound(null, null);
            notificationManagerCompat.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, ContentActivity.class);
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, flags);
        Notification notification = new NotificationCompat.Builder(context.getApplicationContext(), id)
                .setAutoCancel(false)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.app_running))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_logo))
                .setSmallIcon(R.mipmap.ic_logo)
                .setContentIntent(pendingIntent)
                .setSound(null)
                .build();
        return notification;
    }

    @Override
    public void onDestroy() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplication());
        notificationManagerCompat.cancel(NOTIFY_ID);
        stopForeground(true);
        super.onDestroy();
    }
}