package com.example.tm18app.viewModels;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.pojos.Post;
import com.example.tm18app.repository.PostItemRepository;
import com.example.tm18app.util.SingleLiveEvent;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A {@link ViewModel} class representing the ViewModel for the {@link com.example.tm18app.fragment.NewPostFragment} View
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class NewPostViewModel extends ViewModel {

    public MutableLiveData<String> title = new MutableLiveData<>();
    public MutableLiveData<String> content = new MutableLiveData<>();
    public SingleLiveEvent<Boolean> selectContentImage = new SingleLiveEvent<>();

    private Context appContext;
    private String selectedGoal;
    private String contentImageBase64Data;

    private MutableLiveData<Integer> postLiveDataResponse = new MutableLiveData<>();

    /**
     * Getter for the {@link MutableLiveData} of post creation status
     * @return {@link MutableLiveData}
     */
    public MutableLiveData<Integer> getPostLiveDataResponse(){
        return postLiveDataResponse;
    }

    /**
     * Sets the {@link Context} for this ViewModel
     * @param context {@link Context}
     */
    public void setContext(Context context) {
        this.appContext = context;
        contentImageBase64Data = null;
    }

    /**
     * Method called when the new post button is clicked
     */
    public void onNewPostClicked() {
        if(areInputsValid()){
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
            Post post = new Post(title.getValue(), content.getValue(), userID, goalID);
            if(contentImageBase64Data != null)
                post.setBase64Image(contentImageBase64Data);
            repository.createPost(post,
                    postLiveDataResponse);
        }
    }

    public void onUploadImageClicked() {
        selectContentImage.call();
    }

    /**
     * Checks whether the input fields are valid
     * @return true if valid, false otherwise
     */
    private boolean areInputsValid() {
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

    /**
     * Sets the selected goal from the {@link android.widget.Spinner} to apply for the Post
     * @param goalTag {@link String} the selected goal
     */
    public void setSelectedGoalForPost(String goalTag) {
        this.selectedGoal = goalTag;
    }

    public void setContentImageBase64Data(String contentImageBase64Data) {
        this.contentImageBase64Data = contentImageBase64Data;
    }
}
