package com.example.tm18app.viewModels;

import android.content.SharedPreferences;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.model.Post;
import com.example.tm18app.repository.PostItemRepository;

import java.util.List;

/**
 * A {@link ViewModel} abstract class that represents needed functions for a given Profile UI
 * @see com.example.tm18app.fragment.ProfileFragment
 * @see com.example.tm18app.fragment.OtherProfileFragment
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public abstract class ProfileViewModel extends ViewModel {

    protected String userId;
    protected SharedPreferences prefs;
    protected int pageNumber = -1;
    protected boolean isLoadingMoreItems;
    protected LiveData<Integer> totalPagesLiveData = new MutableLiveData<>();

    protected MutableLiveData<Boolean> reloadTrigger = new MutableLiveData<>();

    /**
     * Upon change on the {@link MutableLiveData} reloadTrigger, the postLiveData is created
     * or updated. The reloadTrigger is actuated when the Profile is loaded and reloaded
     * by a swipe. (Can also be programatically called)
     */
    protected LiveData<List<Post>> postLiveData = Transformations.switchMap(reloadTrigger, new Function<Boolean, LiveData<List<Post>>>() {
        @Override
        public LiveData<List<Post>> apply(Boolean input) {
            PostItemRepository postItemRepository = new PostItemRepository();
            ProfileViewModel.this.totalPagesLiveData =
                    postItemRepository.getPagesNumberForPosts(userId,
                            prefs.getString(Constant.PUSHY_TOKEN, ""));
            return postItemRepository.getUserPosts(userId, String.valueOf(pageNumber),
                    prefs.getString(Constant.PUSHY_TOKEN, ""));
        }
    });
    private boolean videoOnFullscreen;
    private String videoUrl;
    private long seekPoint;
    private List<Post> postsList;

    public LiveData<Integer> getTotalPagesLiveData() {
        return totalPagesLiveData;
    }

    public LiveData<List<Post>> getPostLiveData() {
        return postLiveData;
    }

    public void callRepositoryForPosts() {
        reloadTrigger.setValue(true);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPreferences(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public boolean isLoadingMoreItems() {
        return isLoadingMoreItems;
    }

    public void setLoadingMoreItems(boolean loadingMoreItems) {
        isLoadingMoreItems = loadingMoreItems;
    }

    public void setFullScreen(boolean fullScreen){
        this.videoOnFullscreen = fullScreen;
    }

    public void setVideoUrlFullScreen(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setVideoPosFullScreen(long seekPoint) {
        this.seekPoint = seekPoint;
    }

    public void setCurrentPostsList(List<Post> mPostsList) {
        this.postsList = mPostsList;
    }

    public boolean isVideoOnFullscreen() {
        return videoOnFullscreen;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public long getSeekPoint() {
        return seekPoint;
    }

    public List<Post> getPostsList() {
        return postsList;
    }
}

