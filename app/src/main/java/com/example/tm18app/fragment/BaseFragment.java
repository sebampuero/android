package com.example.tm18app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.tm18app.App;
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
        mMainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        mPrefs = getContext().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
    }

    /**
     * Sets up views for the {@link Fragment}
     */
    protected void setupViews() {
        mBottomNavigationView = ((MainActivity)getActivity()).getmBottonNavigationView();
        mToolbar = ((MainActivity)getActivity()).getToolbar();
        mToolbar.setVisibility(View.VISIBLE);
        mBottomNavigationView.setVisibility(View.VISIBLE);
        mToolbar.getMenu().clear();
    }
}
