package com.example.tm18app.fragment;

import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tm18app.adapters.PostItemAdapter;
import com.example.tm18app.model.Post;

import java.util.ArrayList;
import java.util.List;

public abstract class BasePostsContainerFragment extends BaseFragment {

    protected RecyclerView mRecyclerView;
    protected PostItemAdapter mAdapter;
    protected SwipeRefreshLayout mSwipe;
    protected List<Post> mPostsList = new ArrayList<>();

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

    protected abstract void setupRecyclerView();

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mAdapter != null)
            mAdapter.releasePlayers();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mAdapter != null)
            mAdapter.pausePlayers();
    }

}
