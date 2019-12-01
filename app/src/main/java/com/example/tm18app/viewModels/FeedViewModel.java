package com.example.tm18app.viewModels;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.pojos.Post;
import com.example.tm18app.repository.PostItemRepository;

import java.util.Arrays;
import java.util.List;

public class FeedViewModel extends ViewModel {

    private NavController navController;
    private Context appContext;

    private PostItemRepository postItemRepository;
    private LiveData<List<Post>> postLiveData;


    public LiveData<List<Post>> getPostLiveData() {
        return postLiveData;
    }

    public void setNavController(NavController navController) {
        this.navController = navController;
    }

    public void setContext(FragmentActivity activity) {
        this.appContext = activity.getApplicationContext();
    }

    public void fetchData() {
        postItemRepository = new PostItemRepository();
        SharedPreferences preferences = appContext
                .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        if(preferences.getString(Constant.GOAL_IDS, null) != null){
            String[] goalIds = preferences.getString(Constant.GOAL_IDS, null).split(",");
            this.postLiveData = postItemRepository.getPosts(Arrays.asList(goalIds));
        }
    }

    public void onNewPostButtonClicked() {
        navController.navigate(R.id.action_feedFragment_to_newPostFragment);
    }


}
