package com.example.tm18app.viewModels;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.model.User;
import com.example.tm18app.repository.PostItemRepository;
import com.example.tm18app.repository.UserRepository;

public class OtherUserProfileViewModel extends ProfileViewModel {

    private User otherUser;
    private LiveData<User> userLiveData;
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

    public void setPrefs(SharedPreferences prefs) {
        this.prefs = prefs;
    }
}
