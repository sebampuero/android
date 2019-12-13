package com.example.tm18app.viewModels;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.R;
import com.example.tm18app.fragment.WebviewFragment;
import com.example.tm18app.pojos.Post;
import com.example.tm18app.pojos.User;
import com.example.tm18app.repository.PostItemRepository;
import com.example.tm18app.repository.UserRepository;

import java.util.List;

/**
 * A {@link ViewModel} class representing the ViewModel for the {@link com.example.tm18app.fragment.ProfileFragment} View
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class ProfileViewModel extends ViewModel {

    public MutableLiveData<String> names = new MutableLiveData<>();
    public MutableLiveData<String> email = new MutableLiveData<>();
    public MutableLiveData<String> goalsList = new MutableLiveData<>();

    private NavController navController;
    private Context appContext;

    private LiveData<List<Post>> postLiveData;

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
     * Sets the {@link Context} for this ViewModel
     * @param context {@link Context}
     */
    public void setContext(Context context) {
        this.appContext = context;
        fetchData();
        fillUserData();
    }

    /**
     * Populates the {@link android.widget.TextView} on the profile UI with the user's data
     */
    private void fillUserData() {
        SharedPreferences preferences = appContext
                .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        String name = preferences.getString(Constant.NAME, null);
        String lastname = preferences.getString(Constant.LASTNAME, null);
        names.setValue(name + " " + lastname);
        email.setValue(preferences.getString(Constant.EMAIL, null));
        goalsList.setValue(preferences.getString(Constant.GOAL_TAGS, null));
    }

    /**
     * Calls the repository and fetches the user's posts from the server
     */
    private void fetchData() {
        PostItemRepository postItemRepository = new PostItemRepository();
        SharedPreferences preferences = appContext
                .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        String userId = String.valueOf(preferences.getInt(Constant.USER_ID, 0));
        this.postLiveData = postItemRepository.getUserPosts(userId);
    }


    /**
     * Navigate to the edit profile UI
     * @see com.example.tm18app.fragment.EditProfileFragment
     */
    public void onEditInfoClicked() {
        navController.navigate(R.id.action_profileFragment_to_editProfileFragment);
    }

    public void onProfilePicClicked() {
        SharedPreferences preferences = appContext
                .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        Bundle bundle = new Bundle();
        bundle.putString(WebviewFragment.IMG_URL,
                preferences.getString(Constant.PROFILE_PIC_URL, ""));
        navController.navigate(R.id.action_profileFragment_to_webviewFragment, bundle);
    }

}

