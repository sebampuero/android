package com.example.tm18app.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import me.pushy.sdk.Pushy;
import me.pushy.sdk.model.PushyDeviceCredentials;

/**
 * UserPushyTokenAsyncTask retrieves the {@link Pushy} credentials associated with the user
 * @see PushyDeviceCredentials
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class UserPushyTokenAsyncTask extends AsyncTask<String, Void, String> {

    private WeakReference<Context> mContext;

    public UserPushyTokenAsyncTask(Context applicationContext) {
        this.mContext = new WeakReference<>(applicationContext);
    }

    protected String doInBackground(String... params) {
        try {
            URL urlObj = new URL(params[0]);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // connection ok
                BufferedReader in = new BufferedReader(new InputStreamReader( con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                // Schema of the received information is in JSON format and has to be formatted
                JSONObject jsonObject = new JSONObject(response.toString());
                String token = jsonObject.getString("pushy_token");
                String authKey = jsonObject.getString("pushy_auth_key");
                PushyDeviceCredentials credentials = new PushyDeviceCredentials(token, authKey);
                Pushy.setDeviceCredentials(credentials, mContext.get());
                return "OK";
            } else {
                return null;
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
        return null;
    }

}
