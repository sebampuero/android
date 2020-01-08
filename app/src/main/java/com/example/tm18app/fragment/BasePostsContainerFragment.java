package com.example.tm18app.fragment;

import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tm18app.adapters.PostItemAdapter;
import com.example.tm18app.model.Post;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} abstract subclass. This class is responsible for managing UI's that
 * contain {@link Post} elements.
 *
 * @author Sebastian Ampuero
 * @version  1.0
 * @since 05.12.2019
 */
public abstract class BasePostsContainerFragment extends BaseFragment {

    protected RecyclerView mRecyclerView;
    protected PostItemAdapter mAdapter;
    protected SwipeRefreshLayout mSwipe;
    protected List<Post> mPostsList = new ArrayList<>();
    protected RelativeLayout mVideoRL;
    protected PlayerView mSurfaceView;
    protected SimpleExoPlayer mExoPlayer;

    /**
     * Sets up the {@link RecyclerView} containing post items.
     */
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
        if(mExoPlayer != null)
            mExoPlayer.release();
    }

}
