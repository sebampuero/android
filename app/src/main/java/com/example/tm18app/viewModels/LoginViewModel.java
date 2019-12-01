package com.example.tm18app.viewModels;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.R;
import com.example.tm18app.network.RetrofitNetworkConnectionSingleton;
import com.example.tm18app.network.UserRestInterface;
import com.example.tm18app.pojos.User;

import java.io.IOException;
import java.lang.ref.WeakReference;

import retrofit2.Call;
import retrofit2.Response;


public class LoginViewModel extends ViewModel {

    public MutableLiveData<String> email = new MutableLiveData<>();
    public MutableLiveData<String> password = new MutableLiveData<>();
    private NavController navController;

    private Context ctx;

    public void setContext(Context ctx){
        this.ctx = ctx;
    }

    public void setNavController(NavController navController) {
        this.navController=navController;
    }

    public void onLogin() {
        if(isLoginValid())
            new UserLoginAsyncTask(navController, ctx).execute(email.getValue(),
                                                                password.getValue());
    }

    private boolean isLoginValid(){
        if(email.getValue() == null || password.getValue() == null){
            Toast.makeText(ctx, ctx.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(email.getValue().trim().equals("") || password.getValue().trim().equals("")){
            Toast.makeText(ctx, ctx.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }else if(!email.getValue().trim().contains("@")){
            Toast.makeText(ctx, ctx.getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //TODO: Remove this async task from here, take concept from new post fragment

    static class UserLoginAsyncTask extends AsyncTask<String, String, User>{

         NavController navController;
         int statusCode;
         WeakReference<Context> appContext;
         UserRestInterface restClient;

        UserLoginAsyncTask(NavController navController, Context appContext) {
            this.navController = navController;
            this.appContext = new WeakReference<>(appContext);
            RetrofitNetworkConnectionSingleton retrofitNetworkConnectionSingleton = RetrofitNetworkConnectionSingleton.getInstance();
            restClient = retrofitNetworkConnectionSingleton.retrofitInstance().create(UserRestInterface.class);
        }


        @Override
        protected User doInBackground(String... strings) {
            User user = new User();
            user.setEmail(strings[0]);
            user.setPassword(strings[1]);
            Call<User> call = restClient.loginUser(user);
            Response<User> response = null;
            try {
                response = call.execute();
                if(response.code() == 403){
                    statusCode = response.code();
                    return null;
                }
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
        protected void onProgressUpdate(String... values) {
            Toast.makeText(appContext.get(), appContext.get().getString(R.string.logging_in), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(User user) {
            SharedPreferences sharedPreferences = appContext.get().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if(user == null && statusCode == 403) {
                Toast.makeText(appContext.get(), appContext.get().getString(R.string.invalid_credentials), Toast.LENGTH_LONG).show();
            }
            else if(user == null && statusCode == 500) {
                Toast.makeText(appContext.get(), appContext.get().getString(R.string.server_error), Toast.LENGTH_LONG).show();
            }
            else if(user!=null && statusCode == 200) {
                Log.e("TAG", user.toString());
                editor.putBoolean(Constant.LOGGED_IN, true);
                editor.putString(Constant.NAME, user.getName());
                editor.putString(Constant.LASTNAME, user.getLastname());
                editor.putString(Constant.EMAIL, user.getEmail());
                editor.putInt(Constant.USER_ID, user.getId());
                StringBuilder sb = new StringBuilder();
                StringBuilder sb1 = new StringBuilder();
                for (int i = 0; i < user.getGoals().length; i++) {
                    sb.append(user.getGoals()[i]).append(",");
                    sb1.append(user.getGoalTags()[i]).append(",");
                }
                if(user.getGoalTags().length > 0){
                    editor.putString(Constant.GOAL_IDS, sb.toString());
                    editor.putString(Constant.GOAL_TAGS, sb1.toString());
                }
                editor.apply();
                navController.navigate(R.id.action_global_feedFragment);
            }
        }

    }
}
