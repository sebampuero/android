package com.example.tm18app.network;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.example.tm18app.fragment.SettingsFragment;

import java.io.File;

public class DownloadsManager  {

    private DownloadManager.Request mRequest;
    private Context mContext;

    public DownloadsManager(String URL, Context context) {
        this.mRequest = new DownloadManager.Request(Uri.parse(URL));
        this.mContext = context;
    }

    public DownloadsManager setTitle(String title){
        mRequest.setTitle(title);
        return this;
    }

    public DownloadsManager setNotificationVisibility(int visibility){
        mRequest.setNotificationVisibility(visibility);
        return this;
    }

    public DownloadsManager setFilenameForImg(String filename){
        String appName = mContext.getApplicationInfo()
                .loadLabel(mContext.getPackageManager()).toString();
        Log.d(getClass().getSimpleName(), "Directory being " + Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .getAbsolutePath() + "/" + appName + "/");
        File direct =
                new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .getAbsolutePath() + "/" + appName + "/");
        if (!direct.exists()) {
            direct.mkdir();
            Log.d(getClass().getSimpleName(), "dir created for first time");
        }
        mRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,
                File.separator + appName + File.separator + filename);
        return this;
    }

    public void download(){
        SharedPreferences prefs =
                mContext.getSharedPreferences(SettingsFragment.SETTINGS_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        boolean allowedOnMobile = prefs.getBoolean("allow_downloads_mobile_net", false);
        if(allowedOnMobile)
            mRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
                    | DownloadManager.Request.NETWORK_WIFI);
        else
            mRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        DownloadManager manager =
                (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(mRequest);
    }
}
