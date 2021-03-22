package com.example.lunch;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyNotification extends BroadcastReceiver {

    public MyNotification(){}

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 , intent1, 0);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context,"1")
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Some")
                .setContentText("Other")
                .setAutoCancel(true)
                .setVibrate(new long[] {0, 1000, 200,1000 })
                .setLights(Color.MAGENTA, 500, 500)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(200,notification.build());
    }
}