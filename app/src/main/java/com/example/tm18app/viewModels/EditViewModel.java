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
import com.example.tm18app.pojos.Goal;
import com.example.tm18app.pojos.User;
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

    public MutableLiveData<String> name = new MutableLiveData<>();
    public MutableLiveData<String> lastname = new MutableLiveData<>();
    public MutableLiveData<String> email = new MutableLiveData<>();
    public SingleLiveEvent<Boolean> navigateToDialog = new SingleLiveEvent<>(); // https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
    public SingleLiveEvent<Boolean> selectProfilePic = new SingleLiveEvent<>();
    public LiveData<List<Goal>> getGoalLiveData() {
        return goalItemsLiveData;
    }
    public LiveData<HashMap<Integer, User>> getUserLiveData() {
        return userLiveData;
    }

    private LiveData<HashMap<Integer, User>> userLiveData = new MutableLiveData<>();
    private LiveData<List<Goal>> goalItemsLiveData;
    private int userID;
    private MultiGoalSelectAdapter adapter;
    private Context appContext;
    private SharedPreferences preferences;
    private NavController navController;
    private String profilePicBase64Data;

    /**
     * Call repository and fetch goals from the server
     */
    private void fetchGoals() {
        GoalsItemRepository goalsItemRepository = new GoalsItemRepository();
        this.goalItemsLiveData = goalsItemRepository.getGoals();
    }

    /**
     * Event method called when the save button is pressed
     */
    public void onSaveClicked() {
        if(isFormValid()){
            User user = new User();
            user.setId(userID);
            user.setName(name.getValue());
            user.setLastname(lastname.getValue());
            user.setEmail(email.getValue());
            Integer[] goalIds = new Integer[this.adapter.getSelected().size()];
            String[] goalTags = new String[this.adapter.getSelected().size()];
            for(int i = 0; i < this.adapter.getSelected().size(); i++){
                goalIds[i] = this.adapter.getSelected().get(i).getId();
                goalTags[i] = this.adapter.getSelected().get(i).getTag();
            }
            user.setGoals(goalIds);
            user.setGoalTags(goalTags);
            if(profilePicBase64Data != null)
                user.setBase64ProfilePic(profilePicBase64Data);
            UserRepository userRepository = new UserRepository();
            // call the editUser method in repository and pass the MutableLiveData for the UI
            // to observe for changes and show feedback accordingly
            userRepository.editUser(user, (MutableLiveData<HashMap<Integer, User>>) userLiveData);
        }
    }

    /**
     * Initiates the {@link androidx.fragment.app.DialogFragment} for goals requests
     * @see com.example.tm18app.fragment.NewGoalsDialogFragment
     */
    public void onNewGoalsClicked() {
        navigateToDialog.call();
    }

    /**
     * Triggers the observer to open the gallery
     */
    public void onProfilePicUploadClicked() {
        selectProfilePic.call();
    }

    /**
     * Navigates to the UI for password change
     */
    public void onChangePasswordClicked() {
        navController.navigate(R.id.action_editProfileFragment_to_editPasswordFragment);
    }

    /**
     * Checks whether the input fields are valid
     * @return true if valid, false otherwise
     */
    private boolean isFormValid() {
        if(name.getValue() == null
                || lastname.getValue() == null
                || email.getValue() == null){
            Toast.makeText(appContext, appContext.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(name.getValue().trim().equals("")
                || lastname.getValue().trim().equals("")
                || email.getValue().trim().equals("")){
            Toast.makeText(appContext, appContext.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!email.getValue().contains("@")){
            Toast.makeText(appContext, appContext.getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    /**
     * Sets the {@link Context} for this ViewModel
     * @param context {@link Context}
     */
    public void setContext(Context context) {
        this.appContext = context;
        preferences = appContext.getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        fillUserData();
        fetchGoals();
        profilePicBase64Data = null;
    }

    /**
     * Fills the input fields with the user's info
     */
    private void fillUserData() {
        userID = preferences.getInt(Constant.USER_ID, 0);
        name.setValue(preferences.getString(Constant.NAME, null));
        lastname.setValue(preferences.getString(Constant.LASTNAME, null));
        email.setValue(preferences.getString(Constant.EMAIL, null));
    }

    /**
     * Setter for {@link NavController}
     * @param navController
     */
    public void setNavController(NavController navController) {
        this.navController = navController;
    }

    /**
     * Setter for {@link androidx.recyclerview.widget.RecyclerView.Adapter}
     * @param adapter
     */
    public void setAdapter(MultiGoalSelectAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Sets the {@link String} base64 data of the profile picture that is later uploaded to the
     * server
     * @param data {@link String} base64 encoded image data
     */
    public void setProfilePicBase64Data(String data) {
        profilePicBase64Data = data;
    }
}
