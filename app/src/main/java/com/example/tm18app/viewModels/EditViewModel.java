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
import com.example.tm18app.network.EditProfileAsyncTask;
import com.example.tm18app.pojos.Goal;
import com.example.tm18app.pojos.GoalItemSelection;
import com.example.tm18app.pojos.User;
import com.example.tm18app.repository.GoalsItemRepository;
import com.example.tm18app.repository.UserRepository;
import com.example.tm18app.util.SingleLiveEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EditViewModel extends ViewModel {

    public MutableLiveData<String> name = new MutableLiveData<>();
    public MutableLiveData<String> lastname = new MutableLiveData<>();
    public MutableLiveData<String> email = new MutableLiveData<>();
    public SingleLiveEvent<Boolean> navigateToDialog = new SingleLiveEvent<>(); // https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
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

    private void fetchGoals() {
        GoalsItemRepository goalsItemRepository = new GoalsItemRepository();
        this.goalItemsLiveData = goalsItemRepository.getGoals();
    }

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
            UserRepository userRepository = new UserRepository();
            userRepository.editUser(user, (MutableLiveData<HashMap<Integer, User>>) userLiveData);
        }
    }


    public void onNewGoalsClicked() {
        navigateToDialog.call();
    }

    public void onChangePasswordClicked() {
        navController.navigate(R.id.action_editProfileFragment_to_editPasswordFragment);
    }

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


    public void setContext(Context context) {
        this.appContext = context;
        preferences = appContext.getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        fillUserData();
        fetchGoals();
    }

    private void fillUserData() {
        userID = preferences.getInt(Constant.USER_ID, 0);
        name.setValue(preferences.getString(Constant.NAME, null));
        lastname.setValue(preferences.getString(Constant.LASTNAME, null));
        email.setValue(preferences.getString(Constant.EMAIL, null));
    }

    public void setNavController(NavController navController) {
        this.navController = navController;
    }

    public void setAdapter(MultiGoalSelectAdapter adapter) {
        this.adapter = adapter;
    }
}
