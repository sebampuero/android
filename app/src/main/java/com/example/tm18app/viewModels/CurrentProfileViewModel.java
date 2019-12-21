package com.example.tm18app.viewModels;

import android.content.SharedPreferences;

import androidx.navigation.NavController;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.repository.PostItemRepository;

public class CurrentProfileViewModel extends ProfileViewModel {

    private SharedPreferences prefs;
    private NavController navController;

    @Override
    public void callRepositoryForPosts() {
        PostItemRepository postItemRepository = new PostItemRepository();
        String userId = String.valueOf(prefs.getInt(Constant.USER_ID, 0));
        this.postLiveData = postItemRepository.getUserPosts(userId,
                prefs.getString(Constant.PUSHY_TOKEN, ""));
    }

    public void setPrefs(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public void setNavController(NavController navController){
        this.navController = navController;
    }

    /**
     * Navigate to the edit profile UI
     * @see com.example.tm18app.fragment.EditProfileFragment
     */
    public void onEditInfoClicked() {
        navController.navigate(R.id.action_profileFragment_to_editProfileFragment);
    }
}
