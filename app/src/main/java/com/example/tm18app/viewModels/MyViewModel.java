package com.example.tm18app.viewModels;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.R;

public class MyViewModel extends ViewModel {

    private NavController navController;
    private Context cntx;
    private ActionBar actionBar;

    public void setNavController(NavController navController) {
        this.navController=navController;
    }

    public NavController getNavController() {
        return this.navController;
    }

    public void setContext(Context cntx) {
        this.cntx=cntx;
    }

    public Context getContext(){
        return this.cntx;
    }

    /*
    public void setActionBar(ActionBar actionBar){
        this.actionBar = actionBar;
    }
     */

    public ActionBar getActionBar() {
        return this.actionBar;
    }

    public void onLoginPressed(){
        navController.navigate(R.id.action_mainFragment_to_loginFragment);
    }

    public void onRegisterPressed(){
        navController.navigate(R.id.action_mainFragment_to_registrationFragment);
    }

    public void checkLoginStatus(){
        SharedPreferences preferences = cntx.getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        if(!preferences.getBoolean(Constant.LOGGED_IN, false)){
            navController.navigate(R.id.action_feedFragment_to_ftime_nav);
        }
    }

}
