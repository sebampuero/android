package com.example.tm18app.viewModels;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.model.Post;
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

    public MutableLiveData<String> mTitle = new MutableLiveData<>();
    public MutableLiveData<String> mContent = new MutableLiveData<>();
    public SingleLiveEvent<Boolean> mTriggerLoadingBtn = new SingleLiveEvent<>();

    private Context mContext;
    private String mSelectedGoal;

    private String mContentImageURI;
    private String mContentVideoURI;

    /**
     * Sets the {@link Context} for this ViewModel
     * @param context {@link Context}
     */
    public void setContext(Context context) {
        this.mContext = context;
    }

    /**
     * Method called when the new post button is clicked
     */
    public void onNewPostClicked() {
        if(areInputsValid()){
            SharedPreferences prefs = mContext
                    .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
            int userID = prefs.getInt(Constant.USER_ID, 0);
            ArrayList<String> userGoalTags =
                    new ArrayList<>(Arrays.asList(prefs
                            .getString(Constant.GOAL_TAGS, null).split(",")));
            ArrayList<String> userGoalIds =
                    new ArrayList<>(Arrays.asList(prefs
                            .getString(Constant.GOAL_IDS, null).split(",")));
            int goalID = Integer.valueOf(userGoalIds.get(userGoalTags.indexOf(mSelectedGoal)));
            PostItemRepository repository = new PostItemRepository();
            Post post = new Post(mTitle.getValue(), mContent.getValue(), userID, goalID);
            if(mContentImageURI != null)
                post.setContentImageURI(mContentImageURI);
            if(mContentVideoURI != null)
                post.setContentVideoURI(mContentVideoURI);
            repository.createPost(post, prefs.getString(Constant.PUSHY_TOKEN, ""), mContext);
            mTriggerLoadingBtn.call();
        }
    }


    /**
     * Checks whether the input fields are valid
     * @return true if valid, false otherwise
     */
    private boolean areInputsValid() {
        if(mContent.getValue() == null){
            Toast.makeText(mContext, mContext.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(mContent.getValue().trim().equals("")){
            Toast.makeText(mContext, mContext.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }else if(mSelectedGoal == null){
            Toast.makeText(mContext, mContext.getString(R.string.goal_select_for_post), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Sets the selected goal from the {@link android.widget.Spinner} to apply for the Post
     * @param goalTag {@link String} the selected goal
     */
    public void setSelectedGoalForPost(String goalTag) {
        this.mSelectedGoal = goalTag;
    }


    public String getContentImageURI() {
        return mContentImageURI;
    }

    public void setContentImageURI(String contentImageURI) {
        this.mContentImageURI = contentImageURI;
    }

    public String getContentVideoURI() {
        return mContentVideoURI;
    }

    public void setContentVideoURI(String contentVideoURI) {
        this.mContentVideoURI = contentVideoURI;
    }
}
