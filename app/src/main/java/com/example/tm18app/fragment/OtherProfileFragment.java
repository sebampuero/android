package com.example.tm18app.fragment;


import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;
import com.example.tm18app.adapters.PostItemAdapter;
import com.example.tm18app.databinding.FragmentOtherProfileBinding;
import com.example.tm18app.model.Post;
import com.example.tm18app.model.User;
import com.example.tm18app.util.ConverterUtils;
import com.example.tm18app.viewModels.OtherUserProfileViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * A simple {@link Fragment} subclass. Responsible for UI and events for the other user profile UI.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class OtherProfileFragment extends BaseProfileFragment {

    public static final String OTHER_USER_ID = "otherUserID";
    private final String TAG = getClass().getSimpleName();

    private OtherUserProfileViewModel mModel;
    private FragmentOtherProfileBinding mBinding;
    private User otherUser;

    public OtherProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mModel = ViewModelProviders.of(this).get(OtherUserProfileViewModel.class);
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_other_profile, container, false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        setupViews();
        mModel.setPreferences(mPrefs);
        if(mModel.getPageNumber() == -1)
            mModel.setPageNumber(0);
        mModel.callRepositoryForUser(getArguments().getString(OTHER_USER_ID));
        mModel.getUserLiveData().observe(this, user -> {
            ((MainActivity)getActivity()).getToolbar().setTitle(user.getName());
            otherUser = user;
            fillUserData();
            mModel.setUserId(String.valueOf(user.getId()));
            mModel.callRepositoryForPosts();
            fetchData();
        });
        setupRecyclerView();
        return mBinding.getRoot();
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
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mLoadMoreItemsProgressBar.animate().alpha(0).setDuration(200);
                    if(mModel.getPageNumber() > 0)
                        mRecyclerView.scrollBy(0, 100);
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
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        mToolbar.inflateMenu(R.menu.other_profile_menu);
        mToolbar.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.sendPM){
                Bundle b = new Bundle();
                b.putString(ChatMessagesFragment.TO_ID, String.valueOf(otherUser.getId()));
                b.putString(ChatMessagesFragment.TO_NAME, otherUser.getName());
                b.putString(ChatMessagesFragment.PROFILE_PIC, otherUser.getProfilePicUrl());
                mMainModel.getNavController()
                        .navigate(R.id.action_otherProfileFragment_to_chatMessagesFragment, b);
            }
            return false;
        });
        mNoPostsView = mBinding.getRoot().findViewById(R.id.noPostsLayout);
        mProgressBar = mBinding.getRoot().findViewById(R.id.progressBar);
        mProfilePicIW = mBinding.getRoot().findViewById(R.id.profilePic);
        mProgressBar.setVisibility(View.VISIBLE); // show loading animation when posts are being loaded
        mNamesTV = mBinding.getRoot().findViewById(R.id.namesTv);
        mSwipe = mBinding.getRoot().findViewById(R.id.swipeRefreshOtherProfile);
        mSwipe.setOnRefreshListener(() -> {
            mPostsList.clear();
            mModel.setPageNumber(0); // upon swipe call repository again to reload data
            mModel.callRepositoryForPosts();
        });
        mGoalsTvCall = mBinding.getRoot().findViewById(R.id.seeUserGoalsTv);
        mGoalsTvCall.setOnClickListener(goalsInfoClickListener);
        mLoadMoreItemsProgressBar = mBinding.getRoot().findViewById(R.id.loadMoreItemsProgressBar);
    }

    /**
     * Fills views with the user's data
     */
    private void fillUserData() {
        String names = otherUser.getName() + " " + otherUser.getLastname();
        mNamesTV.setText(names);
        userGoals = otherUser.getGoalTags();
        setProfilePic();
    }

    @Override
    protected void setProfilePic() {
        String imgUrl = otherUser.getProfilePicUrl();
        if(imgUrl != null){
            String cacheKey = ConverterUtils.extractUrlKey(imgUrl);
            Picasso.get()
                    .load(imgUrl)
                    .placeholder(R.drawable.ic_person_black_80dp)
                    .stableKey(cacheKey)
                    .into(mProfilePicIW);
        }
    }

    @Override
    protected void setupRecyclerView() {
        mRecyclerView = mBinding.getRoot().findViewById(R.id.postsUserRv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new PostItemAdapter((ArrayList<Post>) mPostsList,
                mMainModel.getNavController(), getContext(), null);
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

}
