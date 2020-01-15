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

    protected String mUserID;
    protected SharedPreferences mPrefs;
    protected int mPageNumber = -1;
    protected boolean mIsLoadingMoreItems;
    protected LiveData<Integer> mTotalPagesLiveData = new MutableLiveData<>();

    protected MutableLiveData<Boolean> mReloadTrigger = new MutableLiveData<>();

    /**
     * Upon change on the {@link MutableLiveData} mReloadTrigger, the mPostLiveData is created
     * or updated. The mReloadTrigger is actuated when the Profile is loaded and reloaded
     * by a swipe. (Can also be programatically called)
     */
    protected LiveData<List<Post>> mPostLiveData = Transformations.switchMap(mReloadTrigger, new Function<Boolean, LiveData<List<Post>>>() {
        @Override
        public LiveData<List<Post>> apply(Boolean input) {
            PostItemRepository postItemRepository = new PostItemRepository();
            ProfileViewModel.this.mTotalPagesLiveData =
                    postItemRepository.getPagesNumberForPosts(mUserID,
                            mPrefs.getString(Constant.PUSHY_TOKEN, ""));
            return postItemRepository.getUserPosts(mUserID, String.valueOf(mPageNumber),
                    mPrefs.getString(Constant.PUSHY_TOKEN, ""));
        }
    });
    private boolean mVideoOnFullScreen;
    private String mVideoUrl;
    private long mSeekPoint;
    private List<Post> mPostsList;

    public LiveData<Integer> getTotalPagesLiveData() {
        return mTotalPagesLiveData;
    }

    public LiveData<List<Post>> getPostLiveData() {
        return mPostLiveData;
    }

    public void callRepositoryForPosts() {
        mReloadTrigger.setValue(true);
    }

    public void setUserId(String userId) {
        this.mUserID = userId;
    }

    public void setPreferences(SharedPreferences prefs) {
        this.mPrefs = prefs;
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

    public void setFullScreen(boolean fullScreen){
        this.mVideoOnFullScreen = fullScreen;
    }

    public void setVideoUrlFullScreen(String videoUrl) {
        this.mVideoUrl = videoUrl;
    }

    public void setVideoPosFullScreen(long seekPoint) {
        this.mSeekPoint = seekPoint;
    }

    public void setCurrentPostsList(List<Post> mPostsList) {
        this.mPostsList = mPostsList;
    }

    public boolean isVideoOnFullscreen() {
        return mVideoOnFullScreen;
    }

    public String getVideoUrl() {
        return mVideoUrl;
    }

    public long getSeekPoint() {
        return mSeekPoint;
    }

    public List<Post> getPostsList() {
        return mPostsList;
    }
}

