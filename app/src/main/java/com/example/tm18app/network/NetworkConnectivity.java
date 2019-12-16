package com.example.tm18app.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.tm18app.fragment.SettingsFragment;

/**
 * Class for cheking network status, type and connectivity.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class NetworkConnectivity {

    public static final String TAG = NetworkConnectivity.class.getSimpleName();

    /**
     * Checks whether the device is using WiFi connection
     * @param context {@link Context}
     * @return true if on WiFi, false if on mobile network or other type of network
     */
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

    /**
     * Checks whether the device has internet connection
     * @param context {@link Context}
     * @return true if there is connection, false otherwise
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Tweaks the quality of an image by network type. If the network type is not WiFi,
     * the image is downloaded with less quality to save data
     * @param context {@link Context}
     * @param imgUrl {@link String} the URL of the image
     * @return {@link String} the tweaked URL of the image for the transformation of a low quality
     * img
     */
    public static String tweakImgQualityByNetworkType(Context context, String imgUrl) {
        SharedPreferences settingsPrefs = context
                .getSharedPreferences(SettingsFragment.SETTINGS_SHARED_PREFERENCES_FILE_NAME,
                        Context.MODE_PRIVATE);
        boolean hasWifi = isWiFiActive(context);
        boolean badQualityOnMobileNetwork =
                settingsPrefs.getBoolean("allow_downloads_mobile_net", false);
        // the structure of the URL is so , that after the "upload" segment some transformations
        // can be made. One of those transformations is the quality tweak that is done by percentage.
        // e.g. URL cloudinary.com/upload/image.jpg is tweaked like this: cloudinary.com/upload/q_50/image.jpg
        // then the image is downloaded with only 50% of the original quality
        String[] splitedPath = imgUrl.split("upload");
        StringBuilder sb = new StringBuilder();
        if(!hasWifi && badQualityOnMobileNetwork){
            sb.append(splitedPath[0]);
            sb.append("upload/");
            sb.append("q_50"); // 50% of image quality
            sb.append(splitedPath[1]);
            imgUrl = sb.toString();
        }
        return imgUrl;
    }

}
