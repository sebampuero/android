package com.example.tm18app.fragment;


import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tm18app.R;
import com.example.tm18app.adapters.PostItemAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.FragmentFeedBinding;
import com.example.tm18app.model.Post;
import com.example.tm18app.viewModels.FeedViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import me.pushy.sdk.Pushy;


/**
 * A simple {@link Fragment} subclass. Responsible for UI and events for the feed section UI.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class FeedFragment extends BasePostsContainerFragment{

    private final String TAG = getClass().getSimpleName();

    private final int ANIMATION_DURATION = 300;

    private FeedViewModel mModel;
    private FragmentFeedBinding mBinding;
    private ProgressBar mProgressBar;
    private ProgressBar mLoadMoreItemsProgressBar;
    private boolean doGoalsExist = true;
    private TextView mNoPostsView;
    private FloatingActionButton mFab;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make Pushy listen for incoming MQTT Notification messages. It only works if writing and
        // reading permissions are granted
        Pushy.listen(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        checkBackBtnPressedFromMainFragment();
        mModel = ViewModelProviders.of(this).get(FeedViewModel.class);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_feed, container, false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        setupViews();
        mModel.setNavController(mMainModel.getNavController());
        setupSwipeRefreshLayout(); // swipe refresh for the possibility to reload posts
        setupRecyclerView();
        mModel.setContext(getContext());
        checkIfGoalsExist();
        if(doGoalsExist){ // if user has selected goals, fetch posts
            if(mModel.getPageNumber() == -1)
                mModel.setPageNumber(0);
            mModel.callRepository();
            fetchData();
        }else{
            mProgressBar.setVisibility(View.GONE);
        }
        return mBinding.getRoot();
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        mProgressBar = mBinding.progressBarFeed;
        mNoPostsView = mBinding.noPostsTv;
        mProgressBar.setVisibility(View.VISIBLE); // to show that posts are loading
        mFab = mBinding.newPostFab;
        mLoadMoreItemsProgressBar = mBinding.loadMoreItemsProgressBar;
    }

    /**
     * Temporary method to prevent going back to the {@link FeedFragment} when the user logs out.
     * This is  a workaround and wouldn't be propery to keep on production
     */
    private void checkBackBtnPressedFromMainFragment() {
        if(!mPrefs.getBoolean(Constant.LOGGED_IN, false)){
            getActivity().finish(); // closes the activity when the app detects the user swipes back
            // when logged out
        }
    }

    /**
     * Checks if the user has selected goals. If otherwise, a {@link Snackbar} is shown explaining
     * that goals can be selected on the Profile.
     */
    private void checkIfGoalsExist() {
        if(mPrefs.getString(Constant.GOAL_TAGS, null) == null){
            Snackbar.make(mBinding.getRoot(), getActivity().getString(R.string.no_goals_msg),
                    Snackbar.LENGTH_LONG).show();
            mNoPostsView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mPostsList.clear();
            doGoalsExist = false;
        }
    }

    /**
     * Sets up the {@link SwipeRefreshLayout} for the Feed UI.
     */
    private void setupSwipeRefreshLayout() {
        mSwipe = mBinding.swipeRefreshLayout;
        mSwipe.setOnRefreshListener(() -> {
            if(mModel.getPostLiveData() != null && doGoalsExist){
                mPostsList.clear();
                mModel.setPageNumber(0);
                mModel.callRepository();
                mAdapter.releasePlayers();
            }else{
                mSwipe.setRefreshing(false);
            }
        });
    }

    /**
     * Sets up Observer to observe changes on the {@link androidx.lifecycle.LiveData} containing
     * a {@link List} of Posts
     */
    private void fetchData() {
        mModel.getPostLiveData().observe(this, posts -> {
            if(posts != null){
                if(posts.size() > 0){
                    if(doGoalsExist){
                        mNoPostsView.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        HashSet<Post> postsSet = new HashSet<>(posts);
                        postsSet.addAll(mPostsList);
                        mPostsList.clear();
                        mPostsList.addAll(postsSet);
                        Collections.sort(mPostsList);
                        mAdapter.notifyDataSetChanged();
                        if(mModel.getPageNumber() > 0)
                            mRecyclerView.scrollBy(0, 100);
                        mLoadMoreItemsProgressBar.animate().alpha(0).setDuration(ANIMATION_DURATION);
                    }
                }else{
                    mNoPostsView.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    mLoadMoreItemsProgressBar.animate().alpha(0).setDuration(ANIMATION_DURATION);
                }
            }else
                Toast.makeText(getContext(),
                        getString(R.string.cannot_load_posts), Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.GONE);
            mSwipe.setRefreshing(false);
            mModel.setLoadingMoreItems(false);
        });
    }

    private PostItemAdapter.PostsEventsListener listener = new PostItemAdapter.PostsEventsListener() {
        @Override
        public void onPostDeleted(MutableLiveData<Integer> statusCode) {
            statusCode.observe(FeedFragment.this, statusCode1 -> {
                handlePostDeletion(statusCode1);
                mModel.callRepository(); // mimic a reload for data
            });
        }

        @Override
        public void onUndoPostDeleted(int itemPosition) {
            mRecyclerView.scrollToPosition(itemPosition);
        }

    };


    @Override
    protected void setupRecyclerView() {
        mRecyclerView = mBinding.rvFeed;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new PostItemAdapter((ArrayList<Post>) mPostsList,
                mMainModel.getNavController(), getContext(), listener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(
                new CustomFeedScrollListener((LinearLayoutManager)mRecyclerView.getLayoutManager()));
    }


    /**
     * Shows feedback to the user about the deletion of the post
     * @param statusCode {@link Integer} status code of the operation
     */
    private void handlePostDeletion(Integer statusCode) {
        if(statusCode == HttpURLConnection.HTTP_INTERNAL_ERROR)
            Toast.makeText(getContext(), getContext().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
    }

    /**
     * Custom scroll listener for the {@link FeedFragment}.
     * @see CustomScrollListener
     */
    class CustomFeedScrollListener extends CustomScrollListener{

        CustomFeedScrollListener(LinearLayoutManager manager) {
            super(manager);
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if(newState == RecyclerView.SCROLL_STATE_IDLE){
                mFab.animate().alpha(1).setDuration(ANIMATION_DURATION);
            }else if(newState == RecyclerView.SCROLL_STATE_DRAGGING){
                // on scrolling, disappear the fab
                mFab.animate().alpha(0).setDuration(ANIMATION_DURATION);
            }
        }

        @Override
        void loadMoreItems() {
            mModel.setPageNumber(mModel.getPageNumber()+1);
            mModel.callRepository();
            mLoadMoreItemsProgressBar.animate().alpha(1).setDuration(ANIMATION_DURATION);
            mModel.setLoadingMoreItems(true);
        }

        @Override
        boolean isLoading() {
            return mModel.isLoadingMoreItems();
        }

        @Override
        boolean lastPageReached() {
            if(mModel.getTotalPagesLiveData().getValue() != null)
                return mModel.getPageNumber() + 1 == mModel.getTotalPagesLiveData().getValue();
            return true;
        }
    }
}
