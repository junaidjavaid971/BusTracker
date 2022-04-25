package com.haroonfazal.haroonapps.bustracker.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.os.Build;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.haroonfazal.haroonapps.bustracker.R;

import java.util.Random;


public class MyFirebaseMessagingService
        extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("Title123", remoteMessage.getNotification().getTitle());
        showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());

    }

    private void showNotification(String title, String body) {
        showNotis(title, body);

    }

    public Notification.Builder builder123;


    private void showNotis(String title, String body) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("channelid1", "001", android.app.NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("This is description");
            android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            builder123 = new Notification.Builder(getApplicationContext(), notificationChannel.getId());
            builder123.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(title);

            NotificationManagerCompat nmc = NotificationManagerCompat.from(getApplicationContext());
            nmc.notify(new Random().nextInt(), builder123.build());
        } else {

            builder123 = new Notification.Builder(getApplicationContext());
            builder123.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setContentText(body);

            NotificationManagerCompat nmc = NotificationManagerCompat.from(getApplicationContext());
            nmc.notify(new Random().nextInt(), builder123.build());
        }
    }
}
