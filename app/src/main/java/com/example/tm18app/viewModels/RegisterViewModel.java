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
import com.example.tm18app.adapters.MultiGoalSelectAdapter;
import com.example.tm18app.network.RetrofitNetworkConnectionSingleton;
import com.example.tm18app.network.UserRestInterface;
import com.example.tm18app.pojos.User;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class RegisterViewModel extends ViewModel {

    public MutableLiveData<String> name = new MutableLiveData<>();
    public MutableLiveData<String> lastname = new MutableLiveData<>();
    public MutableLiveData<String> email = new MutableLiveData<>();
    public MutableLiveData<String> password = new MutableLiveData<>();
    public MutableLiveData<String> passwordConf = new MutableLiveData<>();

    private Context ctx;
    private NavController navController;
    private MultiGoalSelectAdapter adapter;

    private static final String TAG = RegisterViewModel.class.getSimpleName();
    private static boolean DEBUG = true;

    public RegisterViewModel(){
    }

    public void setContext(Context context) {
        this.ctx = context;
    }

    public void setNavController(NavController navController) {
        this.navController = navController;
    }

    public void onRegister(){
        if(isRegisterValid()){
            if(DEBUG){
                for(int i = 0; i < this.adapter.getSelected().size(); i++){
                    Log.d(TAG, this.adapter.getSelected().get(i).toString());
                }
            }
            User user = new User();
            user.setName(name.getValue());
            user.setLastname(lastname.getValue());
            user.setEmail(email.getValue());
            user.setPassword(password.getValue());
            Integer[] goalIds = new Integer[this.adapter.getSelected().size()];
            String[] goalTags = new String[this.adapter.getSelected().size()];
            for(int i = 0; i < this.adapter.getSelected().size(); i++){
                goalIds[i] = this.adapter.getSelected().get(i).getId();
                goalTags[i] = this.adapter.getSelected().get(i).getTag();
             }
             user.setGoals(goalIds);
             user.setGoalTags(goalTags);
             new UserRegisterAsyncTask(navController, ctx, user).execute();
        }
    }

    private boolean isRegisterValid() {
        if(name.getValue() == null
                || lastname.getValue() == null
                || password.getValue() == null
                || passwordConf.getValue() == null
                || email.getValue() == null){
            Toast.makeText(ctx, ctx.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(name.getValue().trim().equals("")
                || lastname.getValue().trim().equals("")
                || password.getValue().trim().equals("")
                || passwordConf.getValue().trim().equals("")
                || email.getValue().trim().equals("")){
            Toast.makeText(ctx, ctx.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!passwordConf.getValue().equals(password.getValue())){
            Toast.makeText(ctx, ctx.getString(R.string.pass_dont_match), Toast.LENGTH_SHORT).show();
            return false;
        }else if(!email.getValue().contains("@")){
            Toast.makeText(ctx, ctx.getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public void setGoalsAdapter(MultiGoalSelectAdapter adapter) {
        this.adapter = adapter;
    }

    static class UserRegisterAsyncTask extends AsyncTask<String, String, User>{
         NavController navController;
         int statusCode;
         Context appContext;
         UserRestInterface restClient;
         User userToRegister;

        UserRegisterAsyncTask(NavController navController, Context appContext, User userToRegister) {
            this.navController = navController;
            this.appContext = appContext;
            RetrofitNetworkConnectionSingleton retrofitNetworkConnectionSingleton = RetrofitNetworkConnectionSingleton.getInstance();
            restClient = retrofitNetworkConnectionSingleton.retrofitInstance().create(UserRestInterface.class);
            this.userToRegister = userToRegister;
        }

        @Override
        protected User doInBackground(String... strings) {
            Call<User> call = restClient.registerUser(userToRegister);
            Response<User> response = null;
            try {
                response = call.execute();
                if(response.code() == 500){
                    statusCode = response.code();
                    return null;
                }
                else if(response.code() == 400){
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
            SharedPreferences sharedPreferences = appContext.getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if(user == null && statusCode == 500) {
                Toast.makeText(appContext, appContext.getString(R.string.server_error), Toast.LENGTH_LONG).show();
            }
            else if(user == null && statusCode == 400){
                Toast.makeText(appContext, appContext.getString(R.string.email_already_exists), Toast.LENGTH_SHORT).show();
            }
            else if(user!=null && statusCode == 200) {
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
                navController.navigate(R.id.action_global_feedFragment);
                if(DEBUG)
                    Log.d(TAG, user.toString());
            }
        }
    }
}
