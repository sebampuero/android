package com.example.tm18app.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.tm18app.fragment.SettingsFragment;

public class NetworkConnectivity {

    public static final String TAG = NetworkConnectivity.class.getSimpleName();

    public static boolean isWiFiActive(Context context) {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiConn = false;
        for (Network network : connMgr.getAllNetworks()) {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                isWifiConn |= networkInfo.isConnected();
            }
        }
        return isWifiConn;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static String tweakImgQualityByNetworkType(Context context, String imgUrl) {
        SharedPreferences settingsPrefs = context
                .getSharedPreferences(SettingsFragment.SETTINGS_SHARED_PREFERENCES_FILE_NAME,
                        Context.MODE_PRIVATE);
        boolean hasWifi = isWiFiActive(context);
        boolean badQualityOnMobileNetwork =
                settingsPrefs.getBoolean("allow_downloads_mobile_net", false);
        String[] splitedPath = imgUrl.split("upload");
        StringBuilder sb = new StringBuilder();
        if(!hasWifi && badQualityOnMobileNetwork){
            Log.d(TAG, "Downloading low quality images");
            sb.append(splitedPath[0]);
            sb.append("upload/");
            sb.append("q_50"); // 50% of image quality
            sb.append(splitedPath[1]);
            imgUrl = sb.toString();
        }
        return imgUrl;
    }

}
