package com.example.tm18app.viewModels;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.model.User;
import com.example.tm18app.repository.PostItemRepository;
import com.example.tm18app.repository.UserRepository;

public class OtherUserProfileViewModel extends ProfileViewModel {

    private LiveData<User> userLiveData;

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public void callRepositoryForUser(String userId) {
        UserRepository repository = new UserRepository();
        this.userLiveData = repository.getUser(userId, prefs.getString(Constant.PUSHY_TOKEN, ""));
    }
}
