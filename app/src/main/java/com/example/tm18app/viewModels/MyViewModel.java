package com.example.tm18app.viewModels;

import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;
import androidx.navigation.NavDeepLinkBuilder;

import com.example.tm18app.MainActivity;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.R;

public class MyViewModel extends ViewModel {

    private NavController navController;
    private Context cntx;

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


    public void onLoginPressed(){
        navController.navigate(R.id.action_mainFragment_to_loginFragment);
    }

    public void onRegisterPressed(){
        navController.navigate(R.id.action_mainFragment_to_registrationFragment);
    }

    public void checkLoginStatus(){
        SharedPreferences preferences = cntx.getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        if(!preferences.getBoolean(Constant.LOGGED_IN, false)) {
            if(navController.getCurrentDestination().getId() == R.id.feedFragment){
                PendingIntent pendingIntent = new NavDeepLinkBuilder(cntx)
                        .setComponentName(MainActivity.class)
                        .setGraph(R.navigation.nav_graph)
                        .setDestination(R.id.mainFragment)
                        .createPendingIntent();
                try {
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
