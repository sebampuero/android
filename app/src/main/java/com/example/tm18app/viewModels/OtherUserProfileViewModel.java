package com.example.tm18app.viewModels;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.lifecycle.LiveData;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.fragment.ProfileImgWebviewFragment;
import com.example.tm18app.model.User;
import com.example.tm18app.repository.PostItemRepository;
import com.example.tm18app.repository.UserRepository;

public class OtherUserProfileViewModel extends ProfileViewModel {

    private User otherUser;
    private LiveData<User> userLiveData;
    private Context context;
    private SharedPreferences prefs;

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public void setOtherUser(User otherUser) {
        this.otherUser = otherUser;
    }

    @Override
    public void callRepositoryForPosts() {
        PostItemRepository postItemRepository = new PostItemRepository();
        String userId = String.valueOf(otherUser.getId());
        this.postLiveData = postItemRepository.getUserPosts(userId,
                prefs.getString(Constant.PUSHY_TOKEN, ""));
    }

    public void callRepositoryForUser(String userId) {
        UserRepository repository = new UserRepository();
        this.userLiveData = repository.getUser(userId, prefs.getString(Constant.PUSHY_TOKEN, ""));
    }

    @Override
    public void onProfilePicClicked() {
        if(otherUser.getProfilePicUrl() != null){
            Bundle bundle = new Bundle();
            String picUrl = otherUser.getProfilePicUrl();
            bundle.putString(ProfileImgWebviewFragment.IMG_URL, picUrl);
            navController.navigate(R.id.action_otherProfileFragment_to_profileImgWebviewFragment, bundle);
        }
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
    }
}
