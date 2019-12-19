package com.example.tm18app.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.navigation.NavDeepLinkBuilder;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;
import com.example.tm18app.fragment.ChatMessagesFragment;
import com.example.tm18app.fragment.ChatsFragment;
import com.example.tm18app.fragment.SettingsFragment;
import com.example.tm18app.model.ChatMessage;

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
        // get shared preferences that are set from configuration view
        SharedPreferences pref =
                context.getSharedPreferences(SettingsFragment.SETTINGS_SHARED_PREFERENCES_FILE_NAME,
                        Context.MODE_PRIVATE);
        // Attempt to extract the property from the payload:
        if (intent.getIntExtra("postId", 0) != 0) {
            if(!pref.getString("notifications_other", "").equals("comments")){
                processCommentNotification(context,
                        intent.getIntExtra("postId", 0),
                        intent.getStringExtra("userName"));
            }
        }else if(intent.getStringExtra("newPostNotificationTag") != null){
            if(!pref.getString("notifications_other", "").equals("posts")){
                processPostNotification(context, intent.getStringExtra("newPostNotificationTag"));
            }
        }else if(intent.getIntExtra("roomId", 0) != 0){
            if(!pref.getString("notifications_other", "").equals("messages")){
                processMessageNotification(context,
                        intent.getIntExtra("roomId", 0),
                        intent.getStringExtra("roomName"),
                        intent.getStringExtra("senderName"),
                        intent.getIntExtra("senderId", 0));
            }
        }
    }



    /**
     * Processes an incoming comment notification
     * @param context {@link Context}
     * @param postId {@link Integer} the post id the comment belongs to
     * @param userName {@link String} the name of the user the comment belongs to
     */
    private void processCommentNotification(Context context, int postId, String userName) {
        Bundle bundle = new Bundle();
        bundle.putString("postID", String.valueOf(postId));
        PendingIntent pendingIntent = new NavDeepLinkBuilder(context)
                .setComponentName(MainActivity.class)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.commentSectionFragment)
                .setArguments(bundle) // so that comment fragment knows what comments to load
                .createPendingIntent();

        String notificationTitle = context.getString(R.string.new_comment_notification);
        String notificationText = userName + " " + context.getString(R.string.new_comment_notification_subtext);

        buildNotification(context, notificationText, notificationTitle, pendingIntent);
    }

    /**
     * Processes an incoming new post notification
     * @param context {@link Context}
     * @param goalTag {@link String} the goal tag that the new post corresponds to
     */
    private void processPostNotification(Context context, String goalTag) {
        PendingIntent pendingIntent = new NavDeepLinkBuilder(context)
                .setComponentName(MainActivity.class)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.feedFragment)
                .createPendingIntent();

        String notificationTitle = context.getString(R.string.new_post_in_group_notification);
        String notificationText =
                context.getString(R.string.new_post_in_group_notification_subtext) + " " + goalTag;

        buildNotification(context, notificationText, notificationTitle, pendingIntent);
    }

    private void processMessageNotification(Context context, int roomId,
                                            String roomName, String senderName, int senderId) {

        Bundle bundle = new Bundle();
        bundle.putString(ChatMessagesFragment.TO_NAME, senderName);
        bundle.putString(ChatMessagesFragment.ROOM_ID, String.valueOf(roomId));
        bundle.putString(ChatMessagesFragment.ROOM_NAME, roomName);
        bundle.putString(ChatMessagesFragment.TO_ID, String.valueOf(senderId));

        PendingIntent pendingIntent = new NavDeepLinkBuilder(context)
                .setComponentName(MainActivity.class)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.chatMessagesFragment)
                .setArguments(bundle)
                .createPendingIntent();

        String notificationTitle = context.getString(R.string.new_message_notification);
        String notificationText =
                senderName + " " + context.getString(R.string.new_message_notification_text);
        buildNotification(context, notificationText, notificationTitle, pendingIntent);
    }


    /**
     * Builds the notification
     * @param context {@link Context}
     * @param notificationText {@link String} the subtext to show on the notification
     * @param notificationTitle {@link String} the setTitle to show on the notification
     * @param pendingIntent {@link PendingIntent} that shows the view when the user clicks on
     *                                           the notification
     */
    private void buildNotification(Context context,
                                   String notificationText,
                                   String notificationTitle,
                                   PendingIntent pendingIntent) {
        // Configure the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.goalsappicon100100)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);

        // Automatically configure a Notification Channel for devices running Android O+
        Pushy.setNotificationChannel(builder, context);

        // Get an instance of the NotificationManager service
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and display it
        notificationManager.notify(1, builder.build());
    }
}
