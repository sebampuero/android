package com.example.tm18app.viewModels;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.pojos.Post;
import com.example.tm18app.repository.PostItemRepository;

import java.util.Arrays;
import java.util.List;

/**
 * A {@link ViewModel} class representing the ViewModel for the {@link com.example.tm18app.fragment.FeedFragment} View
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class FeedViewModel extends ViewModel {

    private NavController navController;
    private Context appContext;

    private MutableLiveData<Boolean> reloadTrigger = new MutableLiveData<>();

    /**
     * Upon change on the {@link MutableLiveData} reloadTrigger, the postLiveData is created
     * or updated. The reloadTrigger is actuated when the Feed is loaded and reloaded
     * by a swipe. (Can also be programatically called)
     */
    private LiveData<List<Post>> postLiveData = Transformations.switchMap(reloadTrigger, new Function<Boolean, LiveData<List<Post>>>() {
        @Override
        public LiveData<List<Post>> apply(Boolean input) {
            PostItemRepository postItemRepository = new PostItemRepository();
            SharedPreferences preferences = appContext
                    .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
            if(preferences.getString(Constant.GOAL_IDS, null) != null){
                String[] goalIds = preferences.getString(Constant.GOAL_IDS, null).split(",");
                return postItemRepository.getPosts(Arrays.asList(goalIds));
            }
            return null;
        }
    });

    /**
     * Getter for the {@link LiveData}
     * @return {@link LiveData}
     */
    public LiveData<List<Post>> getPostLiveData() {
        return postLiveData;
    }

    /**
     * Sets the {@link NavController} for this ViewModel
     * @param navController {@link NavController}
     */
    public void setNavController(NavController navController) {
        this.navController = navController;
    }

    /**
     * Sets the {@link Context} for this ViewModel
     * @param context {@link Context}
     */
    public void setContext(Context context) {
        this.appContext = context;
    }

    /**
     * Calls the repository to fetch and/or update the Posts
     */
    public void callRepository() {
        reloadTrigger.setValue(true);
    }


    /**
     * Navigates to the UI for Post creation
     */
    public void onNewPostButtonClicked() {
        navController.navigate(R.id.action_feedFragment_to_newPostFragment);
    }


}
