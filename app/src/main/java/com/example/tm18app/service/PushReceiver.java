package com.example.tm18app.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.navigation.NavDeepLinkBuilder;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;

import me.pushy.sdk.Pushy;

/**
 * The PushReceiver class is responsible for handling the incoming push notifications.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class PushReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String notificationTitle = context.getString(R.string.new_comment_notification);
        String notificationText = context.getString(R.string.new_comment_notification_subtext);
        int postID = 0;
        String userName = "";

        // Attempt to extract the property from the payload:
        if (intent.getIntExtra("postId", 0) != 0) {
            postID = intent.getIntExtra("postId", 0);
            userName = intent.getStringExtra("userName");
        }

        // Build a pending intent to navigate to the comment section containing the new comment
        Bundle bundle = new Bundle();
        bundle.putString("postID", String.valueOf(postID));
        PendingIntent pendingIntent = new NavDeepLinkBuilder(context)
                .setComponentName(MainActivity.class)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.commentSectionFragment)
                .setArguments(bundle)
                .createPendingIntent();

        // Configure the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setSmallIcon(android.R.drawable.star_on)
                .setContentTitle(notificationTitle)
                .setContentText(userName + " " + notificationText)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);

        // Automatically configure a Notification Channel for devices running Android O+
        Pushy.setNotificationChannel(builder, context);

        // Get an instance of the NotificationManager service
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        // Build the notification and display it
        notificationManager.notify(1, builder.build());
    }
}
