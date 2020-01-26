package com.example.tm18app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.model.UserActivity;
import com.example.tm18app.network.UserActivityAsyncTask;
import com.example.tm18app.viewModels.MyViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Base {@link Fragment} class.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public abstract class BaseFragment extends Fragment {
    protected MyViewModel mMainModel;
    protected SharedPreferences mPrefs;
    protected Toolbar mToolbar;
    protected BottomNavigationView mBottomNavigationView;

    public BaseFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainModel = ViewModelProviders.of(requireActivity()).get(MyViewModel.class);
        mPrefs = requireContext().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
    }

    /**
     * Sets up views for the {@link Fragment}
     */
    protected void setupViews() {
        mBottomNavigationView = ((MainActivity)requireActivity()).getmBottonNavigationView();
        mToolbar = ((MainActivity)requireActivity()).getToolbar();
        mToolbar.setVisibility(View.VISIBLE);
        mBottomNavigationView.setVisibility(View.VISIBLE);
        mToolbar.getMenu().clear();
        checkUserActivity();
    }

    /**
     * Checks the activity of the user. Since this Fragment is the Home Fragment, the activity
     * of the user has to be checked upon this fragment's creation. The activity of a user is for
     * instance if he has new chat messages or if there are unread notifications. In that case,
     * the view should be updated to show that activity.
     * @see UserActivity
     */
    private void checkUserActivity() {
        if(mPrefs.getBoolean(Constant.LOGGED_IN, false)){
            // only check if the user is logged in and if the request was not already sent
            new UserActivityAsyncTask(this::updateBottomNavigation)
                    .execute(mPrefs.getString(Constant.PUSHY_TOKEN, ""),
                            String.valueOf(mPrefs.getInt(Constant.USER_ID, 0)));
        }
    }

    /**
     * Updates the {@link com.google.android.material.bottomnavigation.BottomNavigationView} of
     * this App depending on the user activity.
     * @param userActivity {@link UserActivity} containing this user's activity
     */
    private void updateBottomNavigation(UserActivity userActivity) {
        if(getContext() != null){ // to prevent java.lang.IllegalStateException: Fragment not attached
            // to a context. Research about this bug keeps on...
            if(userActivity.isChatActivity())
                mBottomNavigationView.getMenu().getItem(2) // icon for unread messages
                        .setIcon(getResources().getDrawable(R.drawable.ic_chat_vector_important));
            else
                mBottomNavigationView.getMenu().getItem(2) // icon for unread messages
                        .setIcon(getResources().getDrawable(R.drawable.ic_chat_black_24dp));
        }
    }
}
