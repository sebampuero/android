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
    private boolean hasResultsOnPreviousPages;
    protected SharedPreferences prefs;
    protected int pageNumber;
    protected boolean isLoadingMoreItems;

    protected MutableLiveData<Boolean> reloadTrigger = new MutableLiveData<>();
    protected LiveData<List<Post>> postLiveData = Transformations.switchMap(reloadTrigger, new Function<Boolean, LiveData<List<Post>>>() {
        @Override
        public LiveData<List<Post>> apply(Boolean input) {
            PostItemRepository postItemRepository = new PostItemRepository();
            return postItemRepository.getUserPosts(userId, String.valueOf(pageNumber),
                    prefs.getString(Constant.PUSHY_TOKEN, ""));
        }
    });

    /**
     * Getter for the {@link LiveData} for the user's posts that show on the profile
     * @return {@link LiveData}
     */
    public LiveData<List<Post>> getPostLiveData() {
        return postLiveData;
    }

    /**
     * Calls the repository and fetches the user's posts from the server
     */
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

    public boolean hasResultsOnPreviousPages() {
        return hasResultsOnPreviousPages;
    }

    public void setHasResultsOnPreviousPages(boolean hasResultsOnPreviousPages) {
        this.hasResultsOnPreviousPages = hasResultsOnPreviousPages;
    }

    public boolean isLoadingMoreItems() {
        return isLoadingMoreItems;
    }

    public void setLoadingMoreItems(boolean loadingMoreItems) {
        isLoadingMoreItems = loadingMoreItems;
    }
}

