package com.example.tm18app.viewModels;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.fragment.WebviewFragment;
import com.example.tm18app.repository.PostItemRepository;

public class CurrentProfileViewModel extends ProfileViewModel {

    private SharedPreferences prefs;


    public void setPrefs(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    @Override
    public void callRepositoryForPosts() {
        PostItemRepository postItemRepository = new PostItemRepository();
        String userId = String.valueOf(prefs.getInt(Constant.USER_ID, 0));
        this.postLiveData = postItemRepository.getUserPosts(userId);
    }

    @Override
    public void onProfilePicClicked() {
        Bundle bundle = new Bundle();
        String picUrl = prefs.getString(Constant.PROFILE_PIC_URL, "");
        bundle.putString(WebviewFragment.IMG_URL,
                picUrl);
        navController.navigate(R.id.action_profileFragment_to_webviewFragment, bundle);
    }

    /**
     * Navigate to the edit profile UI
     * @see com.example.tm18app.fragment.EditProfileFragment
     */
    public void onEditInfoClicked() {
        navController.navigate(R.id.action_profileFragment_to_editProfileFragment);
    }
}
