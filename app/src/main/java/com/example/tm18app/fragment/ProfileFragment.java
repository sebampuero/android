package com.example.tm18app.fragment;


import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;
import com.example.tm18app.adapters.PostItemAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.FragmentProfileBinding;
import com.example.tm18app.model.Post;
import com.example.tm18app.util.ConverterUtils;
import com.example.tm18app.viewModels.CurrentProfileViewModel;
import com.squareup.picasso.Picasso;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;


/**
 * A simple {@link Fragment} subclass. Responsible for UI and events for the profile UI.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class ProfileFragment extends BaseProfileFragment implements MainActivity.BackPressedListener {

    private final String TAG = getClass().getSimpleName();

    private CurrentProfileViewModel mModel;
    private FragmentProfileBinding mBinding;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mModel = ViewModelProviders.of(this).get(CurrentProfileViewModel.class);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        mModel.setNavController(mMainModel.getNavController());
        setupViews();
        fillUserData();
        mModel.setNavController(mMainModel.getNavController());
        mModel.setUserId(String.valueOf(mPrefs.getInt(Constant.USER_ID, 0)));
        mModel.setPreferences(mPrefs);
        ((MainActivity)getActivity()).setBackPressedListener(this);
        if(!mModel.isVideoOnFullscreen()){
            if(mModel.getPostsList() != null)
                mPostsList = mModel.getPostsList();
            if(mModel.getPageNumber() == -1)
                mModel.setPageNumber(0);
            mModel.callRepositoryForPosts();
            fetchData();
        }else{
            prepareVideoForFullscreenPlayback();
        }
        setupRecyclerView();
        return mBinding.getRoot();
    }

    @Override
    protected void prepareVideoForFullscreenPlayback() {
        super.prepareVideoForFullscreenPlayback();
        mCoordinatorLayout.setVisibility(View.GONE);
    }

    private void fillUserData() {
        String names = mPrefs
                .getString(Constant.NAME,"") + " " + mPrefs.getString(Constant.LASTNAME, "");
        userGoals = mPrefs.getString(Constant.GOAL_TAGS, "").split(",");
        mNamesTV.setText(names);
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        mNoPostsView = mBinding.getRoot().findViewById(R.id.noPostsLayout);
        mProgressBar = mBinding.getRoot().findViewById(R.id.progressBar);
        mProfilePicIW = mBinding.getRoot().findViewById(R.id.profilePic);
        mProgressBar.setVisibility(View.VISIBLE); // show loading animation when posts are being loaded
        mNamesTV = mBinding.getRoot().findViewById(R.id.namesTv);
        mSwipe = mBinding.getRoot().findViewById(R.id.swipeRefreshCurrentProfile);
        mSwipe.setOnRefreshListener(() -> {
            mPostsList.clear();
            mModel.setPageNumber(0);
            mModel.callRepositoryForPosts();
        });
        mGoalsTvCall = mBinding.getRoot().findViewById(R.id.seeUserGoalsTv);
        mGoalsTvCall.setOnClickListener(goalsInfoClickListener);
        mLoadMoreItemsProgressBar = mBinding.getRoot().findViewById(R.id.loadMoreItemsProgressBar);
        mVideoRL = mBinding.videoRelativeLayoutProfile;
        mSurfaceView = mBinding.playerViewFullScreen;
        mCoordinatorLayout = mBinding.coordinator;
    }

    private PostItemAdapter.PostsEventsListener listener = new PostItemAdapter.PostsEventsListener() {
        @Override
        public void onPostDeleted(MutableLiveData<Integer> statusCode) {
            statusCode.observe(ProfileFragment.this, statusCode1
                    -> handlePostDeletion(statusCode1));
        }

        @Override
        public void onUndoPostDeleted(int itemPosition) {
            mRecyclerView.scrollToPosition(itemPosition);
        }

        @Override
        public void onFullscreen(String videoUrl, long seekPoint) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mModel.setFullScreen(true);
            mModel.setVideoUrlFullScreen(videoUrl);
            mModel.setVideoPosFullScreen(seekPoint);
            mModel.setCurrentPostsList(mPostsList);
        }
    };

    @Override
    protected void setupRecyclerView() {
        mRecyclerView = mBinding.getRoot().findViewById(R.id.postsUserRv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new PostItemAdapter((ArrayList<Post>) mPostsList,
                mMainModel.getNavController(), getContext(), listener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new CustomScrollListener((LinearLayoutManager)mRecyclerView.getLayoutManager()) {
            @Override
            void loadMoreItems() {
                mModel.setPageNumber(mModel.getPageNumber()+1);
                mModel.callRepositoryForPosts();
                mLoadMoreItemsProgressBar.animate().alpha(1).setDuration(200);
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
        });
    }

    @Override
    protected long getVideoFullscreenCurrPos() {
        return mModel.getSeekPoint();
    }

    @Override
    protected String getVideoFullscreenUrl() {
        return mModel.getVideoUrl();
    }


    @Override
    protected void fetchData() {
        mModel.getPostLiveData().observe(this, posts -> {
            if(posts != null){
                if(posts.size() > 0){
                    HashSet<Post> postsSet = new HashSet<>(posts);
                    postsSet.addAll(mPostsList);
                    mPostsList.clear();
                    mPostsList.addAll(postsSet);
                    Collections.sort(mPostsList);
                    mAdapter.notifyDataSetChanged();
                    mAdapter.notifyDataSetChanged();
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mLoadMoreItemsProgressBar.animate().alpha(0).setDuration(200);
                }else{
                    mNoPostsView.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    mLoadMoreItemsProgressBar.animate().alpha(0).setDuration(200);
                }
                mModel.setLoadingMoreItems(false);
                mProgressBar.setVisibility(View.GONE);
            }else
                Toast.makeText(getContext(),
                        getString(R.string.cannot_load_posts), Toast.LENGTH_SHORT).show();
            mSwipe.setRefreshing(false);
        });
        setProfilePic();
    }

    @Override
    protected void setProfilePic() {
        String imgUrl = mPrefs.getString(Constant.PROFILE_PIC_URL, null);
        if(imgUrl != null){
            String cacheKey = ConverterUtils.extractUrlKey(imgUrl);
            Picasso.get()
                    .load(mPrefs.getString(Constant.PROFILE_PIC_URL, null))
                    .placeholder(R.drawable.ic_person_black_80dp)
                    .stableKey(cacheKey)
                    .into(mProfilePicIW);
        }
    }

    /**
     * Shows feedback to the user about the deletion of the post
     * @param statusCode {@link Integer} status code of the operation
     */
    private void handlePostDeletion(Integer statusCode) {
        if(statusCode == HttpURLConnection.HTTP_INTERNAL_ERROR)
            Toast.makeText(getContext(), getContext().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if(mModel.isVideoOnFullscreen()){
            mModel.setFullScreen(false);
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public boolean backPressAllowed() {
        return !mModel.isVideoOnFullscreen();
    }
}
