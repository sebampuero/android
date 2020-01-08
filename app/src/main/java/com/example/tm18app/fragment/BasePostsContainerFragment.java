package com.example.tm18app.fragment;

import android.net.Uri;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tm18app.adapters.PostItemAdapter;
import com.example.tm18app.model.Post;
import com.example.tm18app.network.CacheDataSourceFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

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

    protected void prepareVideoForFullscreenPlayback() {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        mExoPlayer = ExoPlayerFactory
                .newSimpleInstance(getContext(), trackSelector);
        mSurfaceView.setUseController(true);
        mSurfaceView.setPlayer(mExoPlayer);
        mExoPlayer.setPlayWhenReady(true);
        mVideoRL.setVisibility(View.VISIBLE);
        mBottomNavigationView.setVisibility(View.GONE);
        mToolbar.setVisibility(View.GONE);
        MediaSource videoSource = new ExtractorMediaSource(Uri.parse(getVideoFullscreenUrl()),
                new CacheDataSourceFactory(getContext(), // init cache params
                        20 * 1024 * 1024, // 20mb
                        50 * 1024 * 1024), // 50mb
                new DefaultExtractorsFactory(), null, null);
        mExoPlayer.prepare(videoSource);
        mExoPlayer.seekTo(getVideoFullscreenCurrPos());
    }

    protected abstract long getVideoFullscreenCurrPos();

    protected abstract String getVideoFullscreenUrl();

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
