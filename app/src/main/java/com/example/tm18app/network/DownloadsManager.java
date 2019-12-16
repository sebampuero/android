package com.example.tm18app.network;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Class responsible for downloads.
 * @see DownloadManager
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class DownloadsManager  {

    private DownloadManager.Request mRequest;
    private Context mContext;

    public DownloadsManager(String URL, Context context) {
        this.mRequest = new DownloadManager.Request(Uri.parse(URL));
        this.mContext = context;
    }

    /**
     * Sets a title for the download notification
     * @param title {@link String}
     * @return {@link DownloadsManager}
     */
    public DownloadsManager setTitle(String title){
        mRequest.setTitle(title);
        return this;
    }

    /**
     * Sets the visibility of the notification tray. If not set, a tray displaying the download
     * status will not show.
     * @param visibility {@link Integer}
     * @return {@link DownloadsManager}
     */
    public DownloadsManager setNotificationVisibility(int visibility){
        mRequest.setNotificationVisibility(visibility);
        return this;
    }

    /**
     * Sets the filename for an image download
     * @param filename {@link String}
     * @return {@link DownloadsManager}
     */
    public DownloadsManager setFilenameForImg(String filename){
        String appName = mContext.getApplicationInfo()
                .loadLabel(mContext.getPackageManager()).toString();
        File direct =
                new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .getAbsolutePath() + "/" + appName + "/");
        if (!direct.exists()) {
            direct.mkdir();
        }
        mRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,
                File.separator + appName + File.separator + filename);
        return this;
    }

    /**
     * Downloads the file
     */
    public void download(){
        DownloadManager manager =
                (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(mRequest);
    }
}
