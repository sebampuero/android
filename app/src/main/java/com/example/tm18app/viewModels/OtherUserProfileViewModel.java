package com.example.tm18app.viewModels;

import androidx.lifecycle.LiveData;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.model.User;
import com.example.tm18app.repository.UserRepository;

/**
 * {@link androidx.lifecycle.ViewModel} class for other user's profile UI
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class OtherUserProfileViewModel extends ProfileViewModel {

    private LiveData<User> mUserLiveData;

    public LiveData<User> getUserLiveData() {
        return mUserLiveData;
    }

    /**
     * Calls the repository to fetch the user's details
     * @param userId {@link String}
     */
    public void callRepositoryForUser(String userId) {
        UserRepository repository = new UserRepository();
        this.mUserLiveData = repository.getUser(userId, mPrefs.getString(Constant.PUSHY_TOKEN, ""));
    }
}
