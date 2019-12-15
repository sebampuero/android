package com.example.tm18app.viewModels;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.fragment.PostImgWebviewFragment;
import com.example.tm18app.fragment.ProfileImgWebviewFragment;
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
        if(!prefs.getString(Constant.PROFILE_PIC_URL, "").equals("")){
            Bundle bundle = new Bundle();
            String picUrl = prefs.getString(Constant.PROFILE_PIC_URL, "");
            bundle.putString(ProfileImgWebviewFragment.IMG_URL, picUrl);
            navController.navigate(R.id.action_profileFragment_to_profileImgWebviewFragment, bundle);
        }
    }

    /**
     * Navigate to the edit profile UI
     * @see com.example.tm18app.fragment.EditProfileFragment
     */
    public void onEditInfoClicked() {
        navController.navigate(R.id.action_profileFragment_to_editProfileFragment);
    }
}
