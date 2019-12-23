package com.example.tm18app.fragment;


import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;
import com.example.tm18app.adapters.PostItemAdapter;
import com.example.tm18app.databinding.FragmentOtherProfileBinding;
import com.example.tm18app.model.Post;
import com.example.tm18app.model.User;
import com.example.tm18app.viewModels.OtherUserProfileViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class OtherProfileFragment extends BaseFragment {

    public static final String OTHER_USER_ID = "otherUserID";

    private OtherUserProfileViewModel mModel;
    private FragmentOtherProfileBinding mBinding;
    private RecyclerView mRecyclerView;
    private PostItemAdapter mAdapter;
    private List<Post> mPostsList = new ArrayList<>();
    private ProgressBar mProgressBar;
    private LinearLayout mNoPostsView;
    private ImageView mProfilePicIW;
    private TextView mNamesTV;
    private TextView mEmailTV;
    private TextView mGoalsTV;
    private User otherUser;
    private SwipeRefreshLayout mSwipe;

    public OtherProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mModel = ViewModelProviders.of(this).get(OtherUserProfileViewModel.class);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_other_profile, container, false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        setupViews();
        mModel.setPreferences(mPrefs);
        mModel.callRepositoryForUser(getArguments().getString(OTHER_USER_ID));
        mModel.getUserLiveData().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                ((MainActivity)getActivity()).getToolbar().setTitle(user.getName());
                otherUser = user;
                fillUserData();
                mModel.setUserId(String.valueOf(user.getId()));
                mModel.callRepositoryForPosts();
                fetchData();
            }
        });
        setupRecyclerView();
        return mBinding.getRoot();
    }

    private void fetchData() {
        mModel.getPostLiveData().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> posts) {
                if(posts != null){
                    if(posts.size() > 0){
                        mPostsList.clear();
                        mPostsList.addAll(posts);
                        Collections.sort(mPostsList);
                        mAdapter.notifyDataSetChanged();
                        mRecyclerView.setVisibility(View.VISIBLE);
                    }else{
                        mNoPostsView.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.GONE);
                    }
                    mProgressBar.setVisibility(View.GONE);
                }
                mSwipe.setRefreshing(false);
            }
        });
    }

    @Override
    protected void setupViews() {
        Toolbar toolbar = ((MainActivity)getActivity()).getToolbar();
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.other_profile_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.sendPM){
                    Bundle b = new Bundle();
                    b.putString(ChatMessagesFragment.TO_ID, String.valueOf(otherUser.getId()));
                    b.putString(ChatMessagesFragment.TO_NAME, otherUser.getName());
                    b.putString(ChatMessagesFragment.PROFILE_PIC, otherUser.getProfilePicUrl());
                    mMainModel.getNavController()
                            .navigate(R.id.action_otherProfileFragment_to_chatMessagesFragment, b);
                }
                return false;
            }
        });
        mNoPostsView = mBinding.getRoot().findViewById(R.id.noPostsLayout);
        mProgressBar = mBinding.getRoot().findViewById(R.id.progressBar);
        mProfilePicIW = mBinding.profilePic;
        mProgressBar.setVisibility(View.VISIBLE); // show loading animation when posts are being loaded
        mNamesTV = mBinding.getRoot().findViewById(R.id.namesTv);
        mEmailTV = mBinding.getRoot().findViewById(R.id.emailTv);
        mGoalsTV = mBinding.getRoot().findViewById(R.id.goalsInfoTv);
        mSwipe = mBinding.getRoot().findViewById(R.id.swipeRefreshOtherProfile);
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mModel.callRepositoryForPosts();
            }
        });
    }

    private void fillUserData() {
        String names = otherUser.getName() + " " + otherUser.getLastname();
        mNamesTV.setText(names);
        mEmailTV.setText(otherUser.getEmail());
        mGoalsTV.setText(Arrays.toString(otherUser.getGoalTags()));
        setProfilePic(otherUser);
    }

    private void setProfilePic(User user) {
        String imgUrl = user.getProfilePicUrl();
        if(imgUrl != null){
            Picasso.get()
                    .load(imgUrl)
                    .resize(300, 300)
                    .centerCrop()
                    .placeholder(R.drawable.progress_img_animation)
                    .into(mProfilePicIW);
        }
    }

    private void setupRecyclerView() {
        mRecyclerView = mBinding.getRoot().findViewById(R.id.goalsUserRv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new PostItemAdapter((ArrayList<Post>) mPostsList,
                mMainModel.getNavController(), this);
        mRecyclerView.setAdapter(mAdapter);
    }

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
