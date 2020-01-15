package com.example.tm18app.viewModels;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import com.example.tm18app.R;
import com.example.tm18app.adapters.MultiGoalSelectAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.model.Goal;
import com.example.tm18app.model.User;
import com.example.tm18app.repository.GoalsItemRepository;
import com.example.tm18app.repository.UserRepository;
import com.example.tm18app.util.SingleLiveEvent;

import java.util.HashMap;
import java.util.List;

/**
 * A {@link ViewModel} class representing the ViewModel for the {@link com.example.tm18app.fragment.EditProfileFragment} View
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class EditViewModel extends ViewModel {

    public MutableLiveData<String> mName = new MutableLiveData<>();
    public MutableLiveData<String> mLastname = new MutableLiveData<>();
    public MutableLiveData<String> mEmail = new MutableLiveData<>();
    public SingleLiveEvent<Boolean> mNavigateToDialog = new SingleLiveEvent<>(); // https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
    public SingleLiveEvent<Boolean> mSelectProfilePic = new SingleLiveEvent<>();
    public SingleLiveEvent<Boolean> mTriggerLoadingBtn = new SingleLiveEvent<>();
    public LiveData<List<Goal>> getGoalLiveData() {
        return mGoalItemsLiveData;
    }
    public LiveData<HashMap<Integer, User>> getUserLiveData() {
        return mUserLiveData;
    }

    private LiveData<HashMap<Integer, User>> mUserLiveData = new MutableLiveData<>();
    private LiveData<List<Goal>> mGoalItemsLiveData;
    private int mUserID;
    private MultiGoalSelectAdapter mAdapter;
    private Context mContext;
    private SharedPreferences mPreferences;
    private NavController mNavController;
    private String mProfilePic64BaseData;

    /**
     * Call repository and fetch goals from the server
     */
    private void fetchGoals() {
        GoalsItemRepository goalsItemRepository = new GoalsItemRepository();
        this.mGoalItemsLiveData =
                goalsItemRepository.getGoals();
    }

    /**
     * Event method called when the save button is pressed
     */
    public void onSaveClicked() {
        if(isFormValid()){
            User user = new User();
            user.setId(mUserID);
            user.setName(mName.getValue());
            user.setLastname(mLastname.getValue());
            user.setEmail(mEmail.getValue());
            Integer[] goalIds = new Integer[this.mAdapter.getSelected().size()];
            String[] goalTags = new String[this.mAdapter.getSelected().size()];
            for(int i = 0; i < this.mAdapter.getSelected().size(); i++){
                goalIds[i] = this.mAdapter.getSelected().get(i).getId();
                goalTags[i] = this.mAdapter.getSelected().get(i).getTag();
            }
            user.setGoals(goalIds);
            user.setGoalTags(goalTags);
            if(mProfilePic64BaseData != null)
                user.setBase64ProfilePic(mProfilePic64BaseData);
            UserRepository userRepository = new UserRepository();
            // call the editUser method in repository and pass the MutableLiveData for the UI
            // to observe for changes and show feedback accordingly
            userRepository.editUser(user,
                    (MutableLiveData<HashMap<Integer, User>>) mUserLiveData,
                    mPreferences.getString(Constant.PUSHY_TOKEN, ""));
            mTriggerLoadingBtn.call();
        }
    }

    /**
     * Initiates the {@link androidx.fragment.app.DialogFragment} for goals requests
     * @see com.example.tm18app.fragment.NewGoalsDialogFragment
     */
    public void onNewGoalsClicked() {
        mNavigateToDialog.call();
    }

    /**
     * Triggers the observer to open the gallery
     */
    public void onProfilePicUploadClicked() {
        mSelectProfilePic.call();
    }

    /**
     * Navigates to the UI for mPassword change
     */
    public void onChangePasswordClicked() {
        mNavController.navigate(R.id.action_editProfileFragment_to_editPasswordFragment);
    }

    /**
     * Checks whether the input fields are valid
     * @return true if valid, false otherwise
     */
    private boolean isFormValid() {
        if(mName.getValue() == null
                || mLastname.getValue() == null
                || mEmail.getValue() == null){
            Toast.makeText(mContext, mContext.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(mName.getValue().trim().equals("")
                || mLastname.getValue().trim().equals("")
                || mEmail.getValue().trim().equals("")){
            Toast.makeText(mContext, mContext.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!mEmail.getValue().contains("@")){
            Toast.makeText(mContext, mContext.getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    /**
     * Sets the {@link Context} for this ViewModel
     * @param context {@link Context}
     */
    public void setContext(Context context) {
        this.mContext = context;
        mPreferences = mContext.getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        fillUserData();
        fetchGoals();
        mProfilePic64BaseData = null;
    }

    /**
     * Fills the input fields with the user's info
     */
    private void fillUserData() {
        mUserID = mPreferences.getInt(Constant.USER_ID, 0);
        mName.setValue(mPreferences.getString(Constant.NAME, null));
        mLastname.setValue(mPreferences.getString(Constant.LASTNAME, null));
        mEmail.setValue(mPreferences.getString(Constant.EMAIL, null));
    }

    /**
     * Setter for {@link NavController}
     * @param navController
     */
    public void setNavController(NavController navController) {
        this.mNavController = navController;
    }

    /**
     * Setter for {@link androidx.recyclerview.widget.RecyclerView.Adapter}
     * @param adapter
     */
    public void setAdapter(MultiGoalSelectAdapter adapter) {
        this.mAdapter = adapter;
    }

    /**
     * Sets the {@link String} base64 data of the profile picture that is later uploaded to the
     * server
     * @param data {@link String} base64 encoded image data
     */
    public void setProfilePicBase64Data(String data) {
        mProfilePic64BaseData = data;
    }
}
