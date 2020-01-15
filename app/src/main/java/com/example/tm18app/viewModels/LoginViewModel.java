package com.example.tm18app.viewModels;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tm18app.R;
import com.example.tm18app.model.User;
import com.example.tm18app.repository.UserRepository;
import com.example.tm18app.util.SingleLiveEvent;

import java.util.HashMap;

/**
 * A {@link ViewModel} class representing the ViewModel for the {@link com.example.tm18app.fragment.LoginFragment} View
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class LoginViewModel extends ViewModel {

    public MutableLiveData<String> mEmail = new MutableLiveData<>();
    public MutableLiveData<String> mPassword = new MutableLiveData<>();
    public SingleLiveEvent<Boolean> mTriggerLoadintBtn = new SingleLiveEvent<>();

    private Context mContext;
    private LiveData<HashMap<Integer, User>> mUserLiveData = new MutableLiveData<>();

    /**
     * Getter for the {@link LiveData}
     * @return {@link LiveData}
     */
    public LiveData<HashMap<Integer, User>> getUserLiveData(){
        return mUserLiveData;
    }

    /**
     * Sets the {@link Context} for this ViewModel
     * @param ctx {@link Context}
     */
    public void setContext(Context ctx){
        this.mContext = ctx;
    }

    /**
     * Method for when the login button is pressed
     */
    public void onLogin() {
        if(isLoginValid()){
            UserRepository userRepository = new UserRepository();
            User user = new User();
            user.setEmail(mEmail.getValue());
            user.setPassword(mPassword.getValue());
            // pass the MutableLiveData event for the LoginFragment to observe and show feedback
            // to the user
            userRepository.loginUser(user, (MutableLiveData<HashMap<Integer, User>>) mUserLiveData);
            // used for the LoginFragment to trigger the button loading animation
            mTriggerLoadintBtn.call();
        }
    }

    /**
     * Checks whether the input fields are valid
     * @return true if valid, false otherwise
     */
    private boolean isLoginValid(){
        if(mEmail.getValue() == null || mPassword.getValue() == null){
            Toast.makeText(mContext, mContext.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(mEmail.getValue().trim().equals("") || mPassword.getValue().trim().equals("")){
            Toast.makeText(mContext, mContext.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }else if(!mEmail.getValue().trim().contains("@")){
            Toast.makeText(mContext, mContext.getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

}
