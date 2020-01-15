package com.example.tm18app.viewModels;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tm18app.R;
import com.example.tm18app.adapters.MultiGoalSelectAdapter;
import com.example.tm18app.model.Goal;
import com.example.tm18app.model.User;
import com.example.tm18app.repository.GoalsItemRepository;
import com.example.tm18app.repository.UserRepository;
import com.example.tm18app.util.SingleLiveEvent;

import java.util.HashMap;
import java.util.List;

/**
 * A {@link ViewModel} class representing the ViewModel for the {@link com.example.tm18app.fragment.RegistrationFragment} View
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class RegisterViewModel extends ViewModel {

    public MutableLiveData<String> mName = new MutableLiveData<>();
    public MutableLiveData<String> mLastname = new MutableLiveData<>();
    public MutableLiveData<String> mEmail = new MutableLiveData<>();
    public MutableLiveData<String> mPassword = new MutableLiveData<>();
    public MutableLiveData<String> mPasswordConf = new MutableLiveData<>();
    public SingleLiveEvent<Boolean> mTriggerLoadingBtn = new SingleLiveEvent<>();
    public SingleLiveEvent<Boolean> mSelectProfilePic = new SingleLiveEvent<>();

    private Context mContext;
    private MultiGoalSelectAdapter mAdapter;
    private LiveData<List<Goal>> mGoalItemsLiveData;
    private LiveData<HashMap<Integer, User>> mUserLiveData = new MutableLiveData<>();
    private String mProfilePicBase64Data;


    public RegisterViewModel(){
    }

    /**
     * Getter for the goals list {@link LiveData}
     * @return {@link LiveData}
     */
    public LiveData<List<Goal>> getGoalLiveData() {
        return mGoalItemsLiveData;
    }

    /**
     * Getter for the user {@link LiveData} response status
     * @return {@link LiveData}
     */
    public LiveData<HashMap<Integer, User>> getUserLiveData(){
        return mUserLiveData;
    }

    /**
     * Calls repository and fetches goals from the server
     */
    private void fetchGoals() {
        GoalsItemRepository goalsItemRepository = new GoalsItemRepository();
        this.mGoalItemsLiveData = goalsItemRepository.getGoals();
    }

    /**
     * Sets the {@link Context} for this ViewModel
     * @param context {@link Context}
     */
    public void setContext(Context context) {
        this.mContext = context;
        fetchGoals();
        mProfilePicBase64Data = null;
    }

    /**
     * Event method for when the register button is pressed
     */
    public void onRegister(){
        if(isRegisterValid()){
            User user = new User();
            user.setName(mName.getValue());
            user.setLastname(mLastname.getValue());
            user.setEmail(mEmail.getValue());
            user.setPassword(mPassword.getValue());
            Integer[] goalIds = new Integer[this.mAdapter.getSelected().size()];
            String[] goalTags = new String[this.mAdapter.getSelected().size()];
            for(int i = 0; i < this.mAdapter.getSelected().size(); i++){
                goalIds[i] = this.mAdapter.getSelected().get(i).getId();
                goalTags[i] = this.mAdapter.getSelected().get(i).getTag();
             }
             user.setGoals(goalIds);
             user.setGoalTags(goalTags);
             if(mProfilePicBase64Data != null)
                 user.setBase64ProfilePic(mProfilePicBase64Data);
            UserRepository userRepository = new UserRepository();
            // pass MutableLiveData to the repository to change for when status of response updates
             userRepository.registerUser(user, (MutableLiveData<HashMap<Integer, User>>) mUserLiveData, this.mContext);
            mTriggerLoadingBtn.call();
        }
    }

    /**
     * Triggers the observer to open gallery
     */
    public void onSelectProfilePic() {
        mSelectProfilePic.call();
    }

    /**
     * Checks whether the input fields for the registration are valid
     * @return true if valid, false otherwise
     */
    private boolean isRegisterValid() {
        if(mName.getValue() == null
                || mLastname.getValue() == null
                || mPassword.getValue() == null
                || mPasswordConf.getValue() == null
                || mEmail.getValue() == null){
            Toast.makeText(mContext, mContext.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(mName.getValue().trim().equals("")
                || mLastname.getValue().trim().equals("")
                || mPassword.getValue().trim().equals("")
                || mPasswordConf.getValue().trim().equals("")
                || mEmail.getValue().trim().equals("")){
            Toast.makeText(mContext, mContext.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!mPasswordConf.getValue().equals(mPassword.getValue())){
            Toast.makeText(mContext, mContext.getString(R.string.pass_dont_match), Toast.LENGTH_SHORT).show();
            return false;
        }else if(!mEmail.getValue().contains("@")){ // vague verification, dont use in production
            Toast.makeText(mContext, mContext.getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    /**
     * Sets the {@link MultiGoalSelectAdapter} for this ViewModel
     * @param adapter {@link MultiGoalSelectAdapter}
     */
    public void setGoalsAdapter(MultiGoalSelectAdapter adapter) {
        this.mAdapter = adapter;
    }

    /**
     * Sets the {@link String} base64 data of the profile picture that is later uploaded to the
     * server
     * @param data {@link String} base64 encoded image data
     */
    public void setProfilePicBase64Data(String data) {
        mProfilePicBase64Data = data;
    }
}
