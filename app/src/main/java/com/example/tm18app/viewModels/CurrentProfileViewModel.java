package com.example.tm18app.viewModels;

import androidx.navigation.NavController;

import com.example.tm18app.R;

public class CurrentProfileViewModel extends ProfileViewModel {

    private NavController navController;

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
