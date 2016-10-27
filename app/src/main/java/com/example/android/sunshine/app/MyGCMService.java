package com.example.android.sunshine.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Audi on 26/10/16.
 */

public class MyGCMService extends FirebaseMessagingService {


    private static final String LOG = "FireBaseMsg";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(LOG, "Message received");
        if (remoteMessage != null) {
            Log.d(LOG, "From: " + remoteMessage.getFrom() + "\nMessage:" + remoteMessage.getData()
                    + "\nNotification: " + remoteMessage.getNotification().getBody());
        }
    }


    void sendNotification(String alert) {


        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent)
                .setContentText(alert);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());

    }
}
