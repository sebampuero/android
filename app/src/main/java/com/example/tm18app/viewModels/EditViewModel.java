package com.example.tm18app.viewModels;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import com.example.tm18app.R;
import com.example.tm18app.adapters.MultiGoalSelectAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.network.EditProfileAsyncTask;
import com.example.tm18app.network.FetchGoalsAsyncTask;
import com.example.tm18app.pojos.GoalItemSelection;
import com.example.tm18app.pojos.User;

import java.util.ArrayList;

public class EditViewModel extends ViewModel {

    public MutableLiveData<String> name = new MutableLiveData<>();
    public MutableLiveData<String> lastname = new MutableLiveData<>();
    public MutableLiveData<String> email = new MutableLiveData<>();
    public MutableLiveData<String> goalRequest = new MutableLiveData<>();

    private int userID;
    private ArrayList<GoalItemSelection> goals;
    private MultiGoalSelectAdapter adapter;
    private Context appContext;
    private SharedPreferences preferences;
    private NavController navController;

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
            new EditProfileAsyncTask(navController, appContext, user, preferences).execute();
        }
    }


    public void onGoalRequestClicked() {
        String goalRequests = goalRequest.getValue();
        if(goalRequests != null){
            if(goalRequests.contains(" ")){
                Toast.makeText(appContext, appContext.getString(R.string.goal_tip_msg), Toast.LENGTH_LONG).show();
                return;
            }
            if(goalRequests.contains(",")){
                goalRequests.split(",");
                //send goal requests
            }else{
                // send goal request
            }
            Toast.makeText(appContext, appContext.getString(R.string.goal_request_toast_msg), Toast.LENGTH_LONG).show();
            goalRequest.setValue("");
        }else{
            Toast.makeText(appContext, appContext.getString(R.string.empty_field_singular), Toast.LENGTH_LONG).show();
        }
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

    public void setGoalsAdapter(MultiGoalSelectAdapter adapter) {
        this.adapter = adapter;
        goals = new ArrayList<>();
        this.adapter.setGoals(goals);
        if(preferences.getString(Constant.GOAL_TAGS, null) == null)
            new FetchGoalsAsyncTask(adapter, appContext).execute();
        else
            new FetchGoalsAsyncTask(adapter,
                    preferences.getString(Constant.GOAL_TAGS, null).split(","),
                    appContext).execute();
    }

    public void setContext(Context context) {
        this.appContext = context;
        preferences = appContext.getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        fillUserData();
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

}
