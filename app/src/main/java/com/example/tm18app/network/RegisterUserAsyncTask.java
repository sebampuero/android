package com.example.tm18app.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.tm18app.pojos.User;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import me.pushy.sdk.Pushy;
import me.pushy.sdk.util.exceptions.PushyException;
import retrofit2.Response;

/**
 * RegisterUserAsyncTask is responsible for handling the asynchronous registration of the user.
 * In addition to that, the token and auth key for {@link Pushy} notifications services are retrieved
 * and stored in the database
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class RegisterUserAsyncTask extends AsyncTask<Void, Void, User> {

    private WeakReference<Context> appContext;
    private User user;
    private UserRestInterface userRestInterface;
    private MutableLiveData<HashMap<Integer, User>> responseMappingMutableLiveData;
    private int statusCode = 0;

    public RegisterUserAsyncTask(Context applicationContext, User user, UserRestInterface userRestInterface, MutableLiveData<HashMap<Integer, User>> responseMappingMutableLiveData) {
        this.appContext = new WeakReference<>(applicationContext);
        this.user = user;
        this.userRestInterface = userRestInterface;
        this.responseMappingMutableLiveData = responseMappingMutableLiveData;
    }

    protected User doInBackground(Void... params) {
        try {
            /*
            try{
                // Assign a unique token to the device and user
                String deviceToken = Pushy.register(appContext.get());
                user.setPushyAuthKey(Pushy.getDeviceCredentials(appContext.get()).authKey);
                user.setPushyToken(deviceToken);
                Log.d("Pushy", "Registered token " + deviceToken);
                Log.d("Pushy", "Registered auth key " + Pushy.getDeviceCredentials(appContext.get()).authKey);
            } catch (PushyException e){
                e.printStackTrace();
            }
             */
            Response<User> response = userRestInterface.registerUser(user).execute();
            statusCode = response.code();
            return response.body();
        }
        catch (Exception exc) {
            return null;
        }

    }

    @Override
    protected void onPostExecute(User user) {
        // Creates a mapping containing the corresponding status code and user
        // status code will never be 0 if there is connection. However, user can be null
        // and that means an error identified with the status code will be shown
        HashMap<Integer, User> responseCodeMapping = new HashMap<>();
        responseCodeMapping.put(statusCode, user);
        responseMappingMutableLiveData.setValue(responseCodeMapping);
    }
}
