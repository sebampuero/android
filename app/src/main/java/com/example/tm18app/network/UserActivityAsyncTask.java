package com.example.tm18app.network;

import android.os.AsyncTask;

import com.example.tm18app.model.UserActivity;

import java.io.IOException;

import retrofit2.Response;

public class UserActivityAsyncTask extends AsyncTask<String,Void, UserActivity> {

    public interface OnUserActivityListener {
        void onUserActivityReceived(UserActivity activity);
    }

    private OnUserActivityListener listener;
    private UserRestInterface mUserRestInterface;

    public UserActivityAsyncTask(OnUserActivityListener listener){
        this.listener = listener;
        this.mUserRestInterface = RetrofitNetworkConnectionSingleton
                .getInstance().retrofitInstance().create(UserRestInterface.class);
    }

    @Override
    protected UserActivity doInBackground(String... strings) {
        String pushyToken = strings[0];
        String userId = strings[1];
        try {
            Response<UserActivity> resp = mUserRestInterface.getUserActivity(userId, pushyToken).execute();
            return resp.body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(UserActivity activity) {
        listener.onUserActivityReceived(activity);
    }
}
