package com.example.tm18app.network;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.tm18app.R;
import com.example.tm18app.pojos.PasswordReset;

import java.io.IOException;
import java.lang.ref.WeakReference;

import retrofit2.Call;
import retrofit2.Response;

public class PasswordResetAsyncTask extends AsyncTask<String, String, String> {

    private WeakReference<Context> appContext;
    private UserRestInterface restClient;
    private PasswordReset passwordReset;
    private int statusCode;

    public PasswordResetAsyncTask(Context appContext, PasswordReset pass) {
        this.appContext = new WeakReference<>(appContext);
        RetrofitNetworkConnectionSingleton retrofitNetworkConnectionSingleton = RetrofitNetworkConnectionSingleton.getInstance();
        restClient = retrofitNetworkConnectionSingleton.retrofitInstance().create(UserRestInterface.class);
        this.passwordReset = pass;
    }

    @Override
    protected String doInBackground(String... strings) {
        Call<Void> call = restClient.updatePassword(passwordReset);
        try {
            Response<Void> response = call.execute();
            statusCode = response.code();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        if(statusCode == 400){
            Toast.makeText(appContext.get(),
                    appContext.get().getString(R.string.old_password_error),
                    Toast.LENGTH_LONG).show();
        }else if(statusCode == 500){
            Toast.makeText(appContext.get(),
                    appContext.get().getString(R.string.server_error),
                    Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(appContext.get(),
                    appContext.get().getString(R.string.password_update_success),
                    Toast.LENGTH_LONG).show();
        }
    }
}
