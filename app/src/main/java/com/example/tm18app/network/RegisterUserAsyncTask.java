package com.example.tm18app.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.devConfig.Config;
import com.example.tm18app.model.User;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;

import me.pushy.sdk.Pushy;
import me.pushy.sdk.util.exceptions.PushyException;
import retrofit2.Response;

/**
 * RegisterUserAsyncTask is responsible for handling the asynchronous registration of the User.
 * In addition to that, the token and auth key for {@link Pushy} notifications services are retrieved
 * and stored in the database
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class RegisterUserAsyncTask extends AsyncTask<Void, Void, User> {

    private final String TAG = getClass().getSimpleName();

    private WeakReference<Context> mContext;
    private User mUser;
    private UserRestInterface mUserRestInterface;
    private MutableLiveData<HashMap<Integer, User>> mResponseMappingLD;
    private int mStatusCOde = 0;

    public RegisterUserAsyncTask(Context applicationContext,
                                 User user,
                                 UserRestInterface userRestInterface,
                                 MutableLiveData<HashMap<Integer, User>> responseMappingMutableLiveData) {
        this.mContext = new WeakReference<>(applicationContext);
        this.mUser = user;
        this.mUserRestInterface = userRestInterface;
        this.mResponseMappingLD = responseMappingMutableLiveData;
    }

    protected User doInBackground(Void... params) {
            try{
                // Firstly verify that the mEmail does not exist before attempting to assign a pushy
                // token
                HttpURLConnection conn;
                URL url = new URL((Config.DEBUG) ? Constant.API_ENDPOINT_LOCAL
                        : Constant.API_ENDPOINT + "/api/users/verifyEmail/" + mUser.getEmail());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "okhttp/3.10.0");
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){ // means there is NO existing mEmail address
                    String deviceToken = Pushy.register(mContext.get());
                    mUser.setPushyAuthKey(Pushy.getDeviceCredentials(mContext.get()).authKey);
                    mUser.setPushyToken(deviceToken);
                    Response<User> response = mUserRestInterface.registerUser(mUser).execute();
                    mStatusCOde = response.code();
                    return response.body();
                }else if(conn.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST){
                    mStatusCOde = conn.getResponseCode();
                    return null;
                }
            } catch (IOException | PushyException e){
                e.printStackTrace();
            }
        return null;
    }

    @Override
    protected void onPostExecute(User user) {
        // Creates a mapping containing the corresponding status code and mUser
        // status code will never be 0 if there is connection. However, mUser can be null
        // and that means an error identified with the status code will be shown
        HashMap<Integer, User> responseCodeMapping = new HashMap<>();
        responseCodeMapping.put(mStatusCOde, user);
        mResponseMappingLD.setValue(responseCodeMapping);
    }
}
