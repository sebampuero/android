package com.example.tm18app.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.navigation.NavController;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.pojos.User;

import java.io.IOException;
import java.lang.ref.WeakReference;

import retrofit2.Call;
import retrofit2.Response;

public class EditProfileAsyncTask extends AsyncTask<String, String, User> {

    private NavController navController;
    private int statusCode;
    private WeakReference<Context> context;
    private UserRestInterface restClient;
    private User userToRegister;
    private SharedPreferences preferences;

    public EditProfileAsyncTask(NavController navController, Context context, User userToRegister, SharedPreferences preferences){
        this.navController = navController;
        this.userToRegister = userToRegister;
        this.context = new WeakReference<>(context);
        this.preferences = preferences;
        RetrofitNetworkConnectionSingleton retrofitNetworkConnectionSingleton = RetrofitNetworkConnectionSingleton.getInstance();
        restClient = retrofitNetworkConnectionSingleton.retrofitInstance().create(UserRestInterface.class);
    }

    @Override
    protected User doInBackground(String... strings) {
        Call<User> call = restClient.updateUser(userToRegister);
        Response<User> response = null;
        try {
            response = call.execute();
            if(response.code() == 500){
                statusCode = response.code();
                return null;
            }
            else{
                statusCode = response.code();
                return response.body();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(User user) {
        SharedPreferences.Editor editor = preferences.edit();
        if(user == null && statusCode == 500) {
            Toast.makeText(context.get(), context.get().getString(R.string.server_error), Toast.LENGTH_LONG).show();
        }else if(user == null && statusCode == 400){
            Toast.makeText(context.get(),
                    context.get().getString(R.string.email_already_exists),
                    Toast.LENGTH_LONG).show();
        }
        else if(user!=null && statusCode == 200) {
            editor.clear();
            editor.putBoolean(Constant.LOGGED_IN, true);
            editor.putString(Constant.NAME, user.getName());
            editor.putString(Constant.LASTNAME, user.getLastname());
            editor.putString(Constant.EMAIL, user.getEmail());
            editor.putInt(Constant.USER_ID, user.getId());
            if(user.getGoals().length > 0 && userToRegister.getGoalTags().length > 0){
                StringBuilder sb = new StringBuilder();
                StringBuilder sb1 = new StringBuilder();
                for (int i = 0; i < user.getGoals().length; i++) {
                    sb.append(user.getGoals()[i]).append(",");
                    sb1.append(userToRegister.getGoalTags()[i]).append(",");
                }
                editor.putString(Constant.GOAL_IDS, sb.toString());
                editor.putString(Constant.GOAL_TAGS, sb1.toString());
            }
            editor.apply();
            Toast.makeText(context.get(), context.get().getString(R.string.profile_edit_success_msg), Toast.LENGTH_SHORT).show();
            navController.navigate(R.id.action_editProfileFragment_to_profileFragment);
        }
    }
}
