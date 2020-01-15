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

    public MutableLiveData<String> mOldPassword = new MutableLiveData<>();
    public MutableLiveData<String> mNewPassword = new MutableLiveData<>();
    public MutableLiveData<String> mNewPasswordConf = new MutableLiveData<>();

    /**
     * Getter for the {@link MutableLiveData} of the response status
     * @return {@link MutableLiveData}
     */
    public MutableLiveData<Integer> getStatusCodeResponseLiveData() {
        return mStatusCodeLiveData;
    }

    private MutableLiveData<Integer> mStatusCodeLiveData = new MutableLiveData<>();
    private Context mContext;

    /**
     * Event method for when the save buttton is clicked
     */
    public void onSaveBtnClicked() {
        if(areFieldsValid()){
            SharedPreferences preferences = mContext.getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
            int userID = preferences.getInt(Constant.USER_ID, 0);
            PasswordReset passwordReset = new PasswordReset(userID, mOldPassword.getValue(), mNewPassword.getValue());
            UserRepository userRepository = new UserRepository();
            userRepository.changeUserPassword(passwordReset,
                    mStatusCodeLiveData,
                    preferences.getString(Constant.PUSHY_TOKEN, ""));
        }
    }

    /**
     * Sets the {@link Context} for this ViewModel
     * @param context {@link Context}
     */
    public void setContext(Context context) {
        mContext = context;
    }

    /**
     * Checks whether the input fields are valid
     * @return true if valid, false otherwise
     */
    private boolean areFieldsValid() {
        if(mOldPassword.getValue() == null || mNewPassword.getValue() == null || mNewPasswordConf.getValue() == null){
            Toast.makeText(mContext, mContext.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }else if(!mNewPasswordConf.getValue().equals(mNewPassword.getValue())){
            Toast.makeText(mContext, mContext.getString(R.string.pass_dont_match), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
