package com.example.tm18app.network;

import android.os.AsyncTask;

import com.example.tm18app.model.UserActivity;

import java.io.IOException;

import retrofit2.Response;

/**
 * AsyncTask to retrieve the user's activity. As Activity is understood the following:
 * <ul>
 *     <li>New chat messages</li>
 *     <li>New notifications</li>
 *     <li>Other type of news for the user</li>
 * </ul>
 * Upon user activity changes, those changes should be reflected on the UI.
 *
 * @see UserActivity
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 15.12.2019
 */
public class UserActivityAsyncTask extends AsyncTask<String,Void, UserActivity> {

    /**
     * Listener for when the user activity is successfully retrieved from the server
     */
    public interface UserActivityListener {
        void onUserActivityReceived(UserActivity activity);
    }

    private UserActivityListener mListener;
    private UserRestInterface mUserRestInterface;

    public UserActivityAsyncTask(UserActivityListener listener){
        this.mListener = listener;
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
        if(activity != null)
            mListener.onUserActivityReceived(activity);
    }
}
