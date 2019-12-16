package com.example.tm18app.network;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.MutableLiveData;

import com.example.tm18app.model.User;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import me.pushy.sdk.Pushy;
import me.pushy.sdk.util.exceptions.PushyException;
import retrofit2.Response;

/**
 * RegisterUserAsyncTask is responsible for handling the asynchronous registration of the mUser.
 * In addition to that, the token and auth key for {@link Pushy} notifications services are retrieved
 * and stored in the database
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class RegisterUserAsyncTask extends AsyncTask<Void, Void, User> {

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
        try {
            try{
                // Assign a unique token to the device and mUser
                String deviceToken = Pushy.register(mContext.get());
                mUser.setPushyAuthKey(Pushy.getDeviceCredentials(mContext.get()).authKey);
                mUser.setPushyToken(deviceToken);
            } catch (PushyException e){
                e.printStackTrace();
            }
            Response<User> response = mUserRestInterface.registerUser(mUser).execute();
            mStatusCOde = response.code();
            return response.body();
        }
        catch (Exception exc) {
            return null;
        }

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
