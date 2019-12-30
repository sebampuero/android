package com.example.tm18app.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;
import com.example.tm18app.exceptions.FileTooLargeException;
import com.example.tm18app.model.Post;
import com.example.tm18app.network.PostRestInterface;
import com.example.tm18app.network.RetrofitNetworkConnectionSingleton;
import com.example.tm18app.util.ConverterUtils;
import com.example.tm18app.util.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;

import retrofit2.Response;

import static com.example.tm18app.App.CHANNEL_ID;

/**
 * Upload service class for posts. When a {@link Post} is to be created, the upload mechanism is
 * held by the service in a background thread. This is done because when uploading large amounts
 * of data (e.g. a video), the process has to be performed in a background thread using a service
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 07.12.2019
 */
public class UploadService extends Service {

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private Handler mHandler;

    private PostRestInterface postRestInterface;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        postRestInterface = RetrofitNetworkConnectionSingleton.
                getInstance().retrofitInstance().create(PostRestInterface.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // extract relevant post data from the IntentService
        String pushyToken = intent.getStringExtra("pushy");
        Post post = new Post(intent.getStringExtra("title"),
                intent.getStringExtra("content"),
                intent.getIntExtra("userID",0),
                intent.getIntExtra("goalID",0));
        if(intent.getStringExtra("imageUri") != null)
            post.setContentImageURI(intent.getStringExtra("imageUri"));
        if(intent.getStringExtra("videoUri") != null)
            post.setContentVideoURI(intent.getStringExtra("videoUri"));
        // An intent to the Main activity for when the user clicks on the notification
        Intent notifIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifIntent, 0);

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getResources().getString(R.string.uploading_post) + " " + post.getTitle())
                .setSmallIcon(R.drawable.goalsappicon100100)
                .setContentIntent(pendingIntent);

        mNotifyManager.notify(1, mBuilder.build());
        // Start a service to upload the post even if the app is closed
        startForeground(1, mBuilder.build());
        new UploaderAsync(post, pushyToken).execute();
        return START_NOT_STICKY;
    }


    /**
     * Asynctask to upload the post
     */
    class UploaderAsync extends AsyncTask<Void, Void, Integer> {

        final int MAX_IMG_HEIGHT = 800;

        Post post;
        String pushyToken;

        public UploaderAsync(Post post, String pushyToken){
            this.post = post;
            this.pushyToken = pushyToken;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try{
                if(post.getContentVideoURI() != null)
                    post.setBase64Video(getDataForVideo(post.getContentVideoURI()));
                if(post.getContentImageURI() != null)
                    post.setBase64Image(getDataForImage(post.getContentImageURI()));
                Response<Void> resp = postRestInterface.newPost(post, pushyToken).execute();
                return resp.code();
            }catch (FileTooLargeException e){
                mBuilder.setContentTitle(e.getMessage());
                mNotifyManager.notify(1, mBuilder.build());
                mHandler.post(new ShowToastInUI(e.getMessage()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer statusCode) {
            if(statusCode == HttpURLConnection.HTTP_OK){
                mBuilder.setContentTitle(getResources().getString(R.string.post_successfully_created) + " " + post.getTitle());
                mNotifyManager.notify(1, mBuilder.build());
                mHandler.post(new ShowToastInUI(getResources()
                        .getString(R.string.post_successfully_created)));
            }else if(statusCode == HttpURLConnection.HTTP_INTERNAL_ERROR){
                mBuilder.setContentTitle(getResources().getString(R.string.server_error));
                mNotifyManager.notify(1, mBuilder.build());
                mHandler.post(new ShowToastInUI(getResources().getString(R.string.server_error)));
            }
            // stops the service
            stopForeground(false);
        }

        /**
         * Converts a {@link Uri} into bytes and then into a 64base encoded {@link String} for a video
         * @param contentVideoURI {@link String}
         * @return {@String} 64base encoded data
         * @throws IOException
         * @throws FileTooLargeException if the video is bigger than 50MB
         */
        private String getDataForVideo(String contentVideoURI) throws IOException, FileTooLargeException {
            InputStream is =  getContentResolver().openInputStream(compressedVideoUri(Uri.parse(contentVideoURI)));
            byte[] videoBytes = ConverterUtils.getBytes(is);
            Log.e("TAG", String.valueOf(videoBytes.length));
            if(videoBytes.length > 50000000)
                throw new FileTooLargeException(getResources().getString(R.string.file_is_too_large));
            return Base64.encodeToString(videoBytes, Base64.DEFAULT);
        }


        /**
         * Converts a {@link Uri} into bytes and then into a 65base encoded {@link String} for an image.
         * Also processes the corresponding {@link Bitmap} to allow only up to a maximal height.
         * @param contentImageURI {@link Uri}
         * @return {@link String} 64base encoded data
         * @throws IOException
         */
        private String getDataForImage(String contentImageURI) throws IOException {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(contentImageURI));
            int height = bitmap.getHeight();
            if(height > MAX_IMG_HEIGHT)
                height = MAX_IMG_HEIGHT;
            Bitmap resizedBitmap = Picasso.get().load(contentImageURI).resize(0, height).centerCrop().get();
            byte[] contentImageBytes = ConverterUtils.getBytes(resizedBitmap);
            return Base64.encodeToString(contentImageBytes, Base64.DEFAULT);
        }
    }

    /**
     * Compresses a video using as input its {@link Uri}
     * @param inputUri {@link Uri} the source path Uri of the video
     * @return {@link Uri} the destination path of the compressed video
     */
    private Uri compressedVideoUri(Uri inputUri) {
        try {
            String filePath = SiliCompressor.with(this).compressVideo(FileUtils.getPath(this, inputUri),
                    FileUtils.getPublicMediaDir(this));
            return Uri.fromFile(new File(filePath));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return inputUri;
    }

    /**
     * Shows a {@link Toast} in the main thread
     */
    class ShowToastInUI implements Runnable {
        String msg;
        ShowToastInUI(String msg) {
            this.msg = msg;
        }
        @Override
        public void run() {
            Toast.makeText(UploadService.this, msg, Toast.LENGTH_LONG).show();
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
