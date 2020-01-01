package com.example.tm18app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.example.tm18app.model.UserActivity;

public class App extends Application {

    public static final String CHANNEL_ID = "exampleServiceChannel";
    private static UserActivity userActivityInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        createUserActivityInstance();
    }

    private void createUserActivityInstance() {
        userActivityInstance = new UserActivity();
    }

    public static UserActivity getUserActivityInstance(){
        return userActivityInstance;
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Example service channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
