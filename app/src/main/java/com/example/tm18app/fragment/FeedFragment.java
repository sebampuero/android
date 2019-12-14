package com.example.tm18app.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tm18app.R;
import com.example.tm18app.adapters.PostItemAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.FragmentFeedBinding;
import com.example.tm18app.network.UserPushyTokenAsyncTask;
import com.example.tm18app.pojos.Post;
import com.example.tm18app.viewModels.FeedViewModel;
import com.example.tm18app.viewModels.MyViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.pushy.sdk.Pushy;


/**
 * A simple {@link Fragment} subclass. Responsible for UI and events for the feed section UI.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class FeedFragment extends Fragment implements PostItemAdapter.OnPostDeleteListener{

    private MyViewModel mMainModel;
    private FeedViewModel mModel;
    private FragmentFeedBinding mBinding;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private PostItemAdapter mAdapter;
    private List<Post> mPostsList = new ArrayList<>();
    private ProgressBar mProgressBar;
    private boolean doGoalsExist = true;
    private LinearLayout mNoPostsView;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        checkBackBtnPressedFromMainFragment();
        mMainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        mModel = ViewModelProviders.of(getActivity()).get(FeedViewModel.class);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_feed, container, false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        mProgressBar = mBinding.progressBarFeed;
        mNoPostsView = mBinding.feedLinearLayout;
        mProgressBar.setVisibility(View.VISIBLE); // to show that posts are loading
        mModel.setNavController(mMainModel.getNavController());
        setupSwipeRefreshLayout(); // swipe refresh for the possibility to reload posts
        setupRecyclerView();
        mModel.setContext(getContext());
        checkIfGoalsExist();
        requestPushyCreds();
        if(doGoalsExist){ // if user has selected goals, fetch posts
            mModel.callRepository();
            fetchData();
        }else{
            mProgressBar.setVisibility(View.GONE);
        }
        return mBinding.getRoot();
    }

    /**
     * Temporary method to prevent going back to the {@link FeedFragment} when the user logs out.
     * This is  a workaround and wouldn't be propery to keep on production
     */
    private void checkBackBtnPressedFromMainFragment() {
        //TODO: Find alternative
        SharedPreferences preferences = getActivity().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        if(!preferences.getBoolean(Constant.LOGGED_IN, false)){
            getActivity().finish(); // closes the activity when the app detects the user swipes back
            // when logged out
        }
    }

    /**
     * Requests the user's {@link me.pushy.sdk.model.PushyDeviceCredentials} from the database to
     * later store them on the device's internal storage.
     * @see me.pushy.sdk.util.PushyPersistence
     */
    private void requestPushyCreds() {
        // Only requests pushy creds when there are no credentials stored on the device.
        // Do not waste network resources
        if (!Pushy.isRegistered(getActivity().getApplicationContext())) {
            SharedPreferences preferences = getActivity().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
            int userID = preferences.getInt(Constant.USER_ID, 0);
            new UserPushyTokenAsyncTask(getActivity().getApplicationContext()).execute(Constant.PUSHY_CREDS_ENDPOINT + userID);
        }
    }

    /**
     * Checks if the user has selected goals. If otherwise, a {@link Snackbar} is shown explaining
     * that goals can be selected on the Profile.
     */
    private void checkIfGoalsExist() {
        SharedPreferences preferences = getActivity().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        if(preferences.getString(Constant.GOAL_TAGS, null) == null){
            Snackbar.make(mBinding.getRoot(), getActivity().getString(R.string.no_goals_msg), Snackbar.LENGTH_LONG).show();
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
        mSwipeRefreshLayout = mBinding.swipeRefreshLayout;
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(mModel.getPostLiveData() != null && doGoalsExist){
                    mModel.callRepository();
                }else{
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    /**
     * Sets up Observer to observe changes on the {@link androidx.lifecycle.LiveData} containing
     * a {@link List} of Posts
     */
    private void fetchData() {
        if(mModel.getPostLiveData() != null){
            mModel.getPostLiveData().observe(this, new Observer<List<Post>>() {
                @Override
                public void onChanged(List<Post> posts) {
                    if(posts.size() > 0){
                        if(doGoalsExist){
                            mPostsList.clear();
                            mPostsList.addAll(posts);
                            Collections.sort(mPostsList);
                            mAdapter.notifyDataSetChanged();
                            mNoPostsView.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }else{
                        mNoPostsView.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.GONE);
                    }
                    mProgressBar.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        }else{
            mProgressBar.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Sets up the {@link RecyclerView} for the Feed UI.
     */
    private void setupRecyclerView() {
        mRecyclerView = mBinding.rvFeed;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new PostItemAdapter((ArrayList<Post>) mPostsList,
                mMainModel.getNavController(), this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onPostDeleted(MutableLiveData<Integer> statusCode) {
        statusCode.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer statusCode) {
                handlePostDeletion(statusCode);
            }
        });
    }

    /**
     * Shows feedback to the user about the deletion of the post
     * @param statusCode {@link Integer} status code of the operation
     */
    private void handlePostDeletion(Integer statusCode) {
        if(statusCode == HttpURLConnection.HTTP_INTERNAL_ERROR){
            Toast.makeText(getContext(), getContext().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
        }else if(statusCode == HttpURLConnection.HTTP_OK){
            Toast.makeText(getContext(), getContext().getString(R.string.post_deleted_msg), Toast.LENGTH_SHORT).show();
        }
        mModel.callRepository(); // mimic a reload for data
    }
}
