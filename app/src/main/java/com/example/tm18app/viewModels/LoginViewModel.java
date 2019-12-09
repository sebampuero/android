package com.example.tm18app.viewModels;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.R;
import com.example.tm18app.network.RetrofitNetworkConnectionSingleton;
import com.example.tm18app.network.UserRestInterface;
import com.example.tm18app.pojos.User;
import com.example.tm18app.repository.UserRepository;
import com.example.tm18app.util.SingleLiveEvent;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

/**
 * A {@link ViewModel} class representing the ViewModel for the {@link com.example.tm18app.fragment.LoginFragment} View
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class LoginViewModel extends ViewModel {

    public MutableLiveData<String> email = new MutableLiveData<>();
    public MutableLiveData<String> password = new MutableLiveData<>();
    public SingleLiveEvent<Boolean> triggerLoadingBtn = new SingleLiveEvent<>();

    private Context ctx;
    private LiveData<HashMap<Integer, User>> userLiveData = new MutableLiveData<>();

    /**
     * Getter for the {@link LiveData}
     * @return {@link LiveData}
     */
    public LiveData<HashMap<Integer, User>> getUserLiveData(){
        return userLiveData;
    }

    /**
     * Sets the {@link Context} for this ViewModel
     * @param ctx {@link Context}
     */
    public void setContext(Context ctx){
        this.ctx = ctx;
    }

    /**
     * Method for when the login button is pressed
     */
    public void onLogin() {
        if(isLoginValid()){
            UserRepository userRepository = new UserRepository();
            User user = new User();
            user.setEmail(email.getValue());
            user.setPassword(password.getValue());
            // pass the MutableLiveData event for the LoginFragment to observe and show feedback
            // to the user
            userRepository.loginUser(user, (MutableLiveData<HashMap<Integer, User>>) userLiveData);
            // used for the LoginFragment to trigger the button loading animation
            triggerLoadingBtn.call();
        }
    }

    /**
     * Checks whether the input fields are valid
     * @return true if valid, false otherwise
     */
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

}
