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
import com.example.tm18app.model.Post;
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

    private int mPageNumber = -1;
    private boolean mIsLoadingMoreItems;
    private NavController mNavController;
    private Context mContext;
    private LiveData<Integer> mTotalPagesLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> mReloadTrigger = new MutableLiveData<>();

    /**
     * Upon change on the {@link MutableLiveData} mReloadTrigger, the mPostLiveData is created
     * or updated. The mReloadTrigger is actuated when the Feed is loaded and reloaded
     * by a swipe. (Can also be programatically called)
     */
    private LiveData<List<Post>> mPostLiveData = Transformations.switchMap(mReloadTrigger, new Function<Boolean, LiveData<List<Post>>>() {
        @Override
        public LiveData<List<Post>> apply(Boolean input) {
            PostItemRepository postItemRepository = new PostItemRepository();
            SharedPreferences preferences = mContext
                    .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
            if(preferences.getString(Constant.GOAL_IDS, null) != null){
                String[] goalIds = preferences.getString(Constant.GOAL_IDS, null).split(",");
                FeedViewModel.this.mTotalPagesLiveData =
                        postItemRepository.getPagesNumberForPosts(Arrays.asList(goalIds),
                                preferences.getString(Constant.PUSHY_TOKEN, ""));
                return postItemRepository.getPosts(Arrays.asList(goalIds),
                        preferences.getString(Constant.PUSHY_TOKEN, ""), String.valueOf(mPageNumber));
            }
            return null;
        }
    });
    private boolean videoOnFullscreen;
    private String videoUrl;
    private long videoCurrPos;
    private List<Post> currentPostsList;

    public LiveData<Integer> getTotalPagesLiveData() {
        return mTotalPagesLiveData;
    }

    public LiveData<List<Post>> getPostLiveData() {
        return mPostLiveData;
    }

    public void setNavController(NavController navController) {
        this.mNavController = navController;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public void callRepository() {
        mReloadTrigger.setValue(true);
    }


    public void onNewPostButtonClicked() {
        mNavController.navigate(R.id.action_feedFragment_to_newPostFragment);
    }

    public int getPageNumber() {
        return mPageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.mPageNumber = pageNumber;
    }


    public boolean isLoadingMoreItems() {
        return mIsLoadingMoreItems;
    }

    public void setLoadingMoreItems(boolean loadingMoreItems) {
        mIsLoadingMoreItems = loadingMoreItems;
    }

    public void setFullScreen(boolean fullscreen) {
        this.videoOnFullscreen = fullscreen;
    }

    public boolean isVideoOnFullscreen() {
        return videoOnFullscreen;
    }

    public void setVideoUrlFullScreen(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setVideoPosFullScreen(long currPos) {
        this.videoCurrPos = currPos;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public long getVideoCurrPos() {
        return videoCurrPos;
    }

    public void setCurrentPostsList(List<Post> mPostsList) {
        this.currentPostsList = mPostsList;
    }

    public List<Post> getCurrentPostsList() {
        return currentPostsList;
    }
}
