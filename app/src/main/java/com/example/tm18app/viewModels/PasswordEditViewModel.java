package com.example.tm18app.viewModels;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.model.PasswordReset;
import com.example.tm18app.repository.UserRepository;

/**
 * A {@link ViewModel} class representing the ViewModel for the {@link com.example.tm18app.fragment.EditPasswordFragment} View
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class PasswordEditViewModel extends ViewModel {

    public MutableLiveData<String> oldPassword = new MutableLiveData<>();
    public MutableLiveData<String> newPassword = new MutableLiveData<>();
    public MutableLiveData<String> newPasswordConf = new MutableLiveData<>();

    /**
     * Getter for the {@link MutableLiveData} of the response status
     * @return {@link MutableLiveData}
     */
    public MutableLiveData<Integer> getStatusCodeResponseLiveData() {
        return statusCodeResponseLiveData;
    }

    private MutableLiveData<Integer> statusCodeResponseLiveData = new MutableLiveData<>();
    private Context appContext;

    /**
     * Event method for when the save buttton is clicked
     */
    public void onSaveBtnClicked() {
        if(areFieldsValid()){
            SharedPreferences preferences = appContext.getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
            int userID = preferences.getInt(Constant.USER_ID, 0);
            PasswordReset passwordReset = new PasswordReset(userID, oldPassword.getValue(), newPassword.getValue());
            UserRepository userRepository = new UserRepository();
            userRepository.changeUserPassword(passwordReset, statusCodeResponseLiveData);
        }
    }

    /**
     * Sets the {@link Context} for this ViewModel
     * @param context {@link Context}
     */
    public void setContext(Context context) {
        appContext = context;
    }

    /**
     * Checks whether the input fields are valid
     * @return true if valid, false otherwise
     */
    private boolean areFieldsValid() {
        if(oldPassword.getValue() == null || newPassword.getValue() == null || newPasswordConf.getValue() == null){
            Toast.makeText(appContext, appContext.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }else if(!newPasswordConf.getValue().equals(newPassword.getValue())){
            Toast.makeText(appContext, appContext.getString(R.string.pass_dont_match), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
