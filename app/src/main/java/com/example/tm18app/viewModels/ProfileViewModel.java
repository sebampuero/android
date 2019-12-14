package com.example.tm18app.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import com.example.tm18app.pojos.Post;

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

    protected NavController navController;

    protected LiveData<List<Post>> postLiveData;

    /**
     * Sets the {@link NavController} for this ViewModel
     * @param navController {@link NavController}
     */
    public void setNavController(NavController navController) {
        this.navController = navController;
    }

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
    public abstract void callRepositoryForPosts();

    /**
     * Called when the {@link android.widget.ImageView} containing the profile picture
     * is clicked
     */
    public abstract void onProfilePicClicked();

}

