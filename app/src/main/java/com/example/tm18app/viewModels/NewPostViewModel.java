package com.example.tm18app.viewModels;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.pojos.Post;
import com.example.tm18app.repository.PostItemRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class NewPostViewModel extends ViewModel {

    public MutableLiveData<String> title = new MutableLiveData<>();
    public MutableLiveData<String> content = new MutableLiveData<>();

    private Context appContext;
    private String selectedGoal;

    private LiveData<HashMap<Integer, String>> postLiveDataResponse = new MutableLiveData<>();

    public LiveData<HashMap<Integer, String>> getPostLiveDataResponse(){
        return postLiveDataResponse;
    }

    public void setContext(FragmentActivity activity) {
        this.appContext = activity.getApplicationContext();
    }

    public void onNewPostClicked() {
        if(isFormValid()){
            SharedPreferences sharedPreferences = appContext
                    .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
            int userID = sharedPreferences.getInt(Constant.USER_ID, 0);
            ArrayList<String> userGoalTags =
                    new ArrayList<>(Arrays.asList(sharedPreferences
                            .getString(Constant.GOAL_TAGS, null).split(",")));
            ArrayList<String> userGoalIds =
                    new ArrayList<>(Arrays.asList(sharedPreferences
                            .getString(Constant.GOAL_IDS, null).split(",")));
            int goalID = Integer.valueOf(userGoalIds.get(userGoalTags.indexOf(selectedGoal)));
            PostItemRepository repository = new PostItemRepository();
            repository.createPost(new Post(title.getValue(), content.getValue(), userID, goalID),
                    (MutableLiveData<HashMap<Integer, String>>) postLiveDataResponse);
            cleanValues();
        }
    }

    private void cleanValues() {
        title.setValue("");
        content.setValue("");
    }

    private boolean isFormValid() {
        if(title.getValue() == null || content.getValue() == null){
            Toast.makeText(appContext, appContext.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(title.getValue().trim().equals("") || content.getValue().trim().equals("")){
            Toast.makeText(appContext, appContext.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }else if(selectedGoal == null){
            Toast.makeText(appContext, appContext.getString(R.string.goal_select_for_post), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void setSelectedGoalForPost(String goalTag) {
        this.selectedGoal = goalTag;
    }
}
