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
            // Assign a unique token to this device
            try{
                String deviceToken = Pushy.register(appContext.get());
                user.setPushyAuthKey(Pushy.getDeviceCredentials(appContext.get()).authKey);
                user.setPushyToken(deviceToken);
                Log.d("Pushy", "Registered token " + deviceToken);
                Log.d("Pushy", "Registered auth key " + Pushy.getDeviceCredentials(appContext.get()).authKey);
            } catch (PushyException e){
                e.printStackTrace();
            }
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
        if(user != null){
            HashMap<Integer, User> responseCodeMapping = new HashMap<>();
            responseCodeMapping.put(statusCode, user);
            responseMappingMutableLiveData.setValue(responseCodeMapping);
        }
    }
}
