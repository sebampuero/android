package com.example.tm18app.viewModels;

import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;
import androidx.navigation.NavDeepLinkBuilder;

import com.example.tm18app.MainActivity;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.R;

/**
 * {@link ViewModel} main VM for the Application
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class MyViewModel extends ViewModel {

    private NavController mNavController;
    private Context mContext;

    /**
     * Sets the {@link NavController} for this ViewModel
     * @param navController {@link NavController}
     */
    public void setNavController(NavController navController) {
        this.mNavController =navController;
    }

    /**
     * Getter for the {@link NavController}
     * @return {@link NavController}
     */
    public NavController getNavController() {
        return this.mNavController;
    }

    /**
     * Sets the {@link Context} for this ViewModel
     * @param cntx
     */
    public void setContext(Context cntx) {
        this.mContext =cntx;
    }

    /**
     * Navigate to the Login UI
     */
    public void onLoginPressed(){
        this.mNavController.navigate(R.id.action_mainFragment_to_loginFragment);
    }

    /**
     * Navigate to the Register UI
     */
    public void onRegisterPressed(){
        this.mNavController.navigate(R.id.action_mainFragment_to_registrationFragment);
    }

    /**
     * Checks if the user is already  logged in upon opening of the App. When logged in, the
     * {@link com.example.tm18app.fragment.FeedFragment} view is opened. When not, the start page
     * {@link com.example.tm18app.fragment.MainFragment} is opened.
     */
    public void checkLoginStatus(){
        SharedPreferences preferences = mContext.getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        if(!preferences.getBoolean(Constant.LOGGED_IN, false)) {
            if(mNavController.getCurrentDestination().getId() == R.id.feedFragment){
                // if we are trying to open feed without login details, create a DeepLink to the
                // main page
                PendingIntent pendingIntent = new NavDeepLinkBuilder(mContext)
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
