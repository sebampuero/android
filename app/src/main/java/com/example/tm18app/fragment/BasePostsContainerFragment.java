package com.example.tm18app.fragment;

import android.view.View;

public class BasePostsContainerFragment extends BaseFragment {

    /**
     * Enables the cinema mode by setting the visibility to gone to the {@link androidx.appcompat.widget.Toolbar}
     * and {@link com.google.android.material.bottomnavigation.BottomNavigationView}
     * @param landscape true if in landscape, false if in portrait
     */
    protected void setCinemaMode(boolean landscape) {
        if(landscape){
            mBottomNavigationView.setVisibility(View.GONE);
            mToolbar.setVisibility(View.GONE);
        }else{
            mToolbar.setVisibility(View.VISIBLE);
            mBottomNavigationView.setVisibility(View.VISIBLE);
        }
    }

}
