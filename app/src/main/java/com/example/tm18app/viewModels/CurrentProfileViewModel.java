package com.example.tm18app.viewModels;

import androidx.navigation.NavController;

import com.example.tm18app.R;

/**
 * {@link androidx.lifecycle.ViewModel} class for the logged in user profile UI
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class CurrentProfileViewModel extends ProfileViewModel {

    private NavController mNavController;

    public void setNavController(NavController navController){
        this.mNavController = navController;
    }
    /**
     * Navigate to the edit profile UI
     * @see com.example.tm18app.fragment.EditProfileFragment
     */
    public void onEditInfoClicked() {
        mNavController.navigate(R.id.action_profileFragment_to_editProfileFragment);
    }
}
