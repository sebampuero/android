package com.example.tm18app.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Base64;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;
import com.example.tm18app.exceptions.FileTooLargeException;
import com.example.tm18app.model.Post;
import com.example.tm18app.network.PostRestInterface;
import com.example.tm18app.network.RetrofitNetworkConnectionSingleton;
import com.example.tm18app.util.ConverterUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import retrofit2.Response;

import static com.example.tm18app.App.CHANNEL_ID;

public class UploadService extends Service {

    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;

    private PostRestInterface postRestInterface;

    @Override
    public void onCreate() {
        super.onCreate();
        postRestInterface = RetrofitNetworkConnectionSingleton.
                getInstance().retrofitInstance().create(PostRestInterface.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String pushyToken = intent.getStringExtra("pushy");
        Post post = new Post(intent.getStringExtra("title"),
                intent.getStringExtra("content"),
                intent.getIntExtra("userID",0),
                intent.getIntExtra("goalID",0));
        if(intent.getStringExtra("imageUri") != null)
            post.setContentImageURI(intent.getStringExtra("imageUri"));
        if(intent.getStringExtra("videoUri") != null)
            post.setContentVideoURI(intent.getStringExtra("videoUri"));
        Intent notifIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifIntent, 0);

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getResources().getString(R.string.uploading_post) + " " + post.getTitle())
                .setSmallIcon(R.drawable.goalsappicon100100)
                .setContentIntent(pendingIntent);

        mNotifyManager.notify(1, mBuilder.build());
        startForeground(1, mBuilder.build());
        new UploaderAsync(post, pushyToken).execute();
        return START_NOT_STICKY;
    }


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
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        private String getDataForImage(String contentImageURI) throws IOException {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(contentImageURI));
            int height = bitmap.getHeight();
            if(height > MAX_IMG_HEIGHT)
                height = MAX_IMG_HEIGHT;
            Bitmap resizedBitmap = Picasso.get().load(contentImageURI).resize(0, height).centerCrop().get();
            byte[] contentImageBytes = ConverterUtils.getBytes(resizedBitmap);
            return Base64.encodeToString(contentImageBytes, Base64.DEFAULT);
        }

        @Override
        protected void onPostExecute(Integer statusCode) {
            if(statusCode == HttpURLConnection.HTTP_OK){
                mBuilder.setContentTitle(getResources().getString(R.string.post_successfully_created));
                mNotifyManager.notify(1, mBuilder.build());
            }else if(statusCode == HttpURLConnection.HTTP_INTERNAL_ERROR){
                mBuilder.setContentTitle(getResources().getString(R.string.server_error));
                mNotifyManager.notify(1, mBuilder.build());
            }
            stopForeground(false);
        }

        private String getDataForVideo(String contentVideoURI) throws IOException, FileTooLargeException {
            InputStream is =  getContentResolver().openInputStream(Uri.parse(contentVideoURI));
            byte[] videoBytes = ConverterUtils.getBytes(is);
            if(videoBytes.length > 50000000) throw new FileTooLargeException(getResources().getString(R.string.file_is_too_large));
            return Base64.encodeToString(videoBytes, Base64.DEFAULT);
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
