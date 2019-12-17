package com.example.tm18app.fragment;


import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
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
import com.example.tm18app.viewModels.MyViewModel;
import com.example.tm18app.viewModels.OtherUserProfileViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class OtherProfileFragment extends Fragment {

    public static final String OTHER_USER_ID = "otherUserID";

    private MyViewModel mMainModel;
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

    public OtherProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mModel = ViewModelProviders.of(getActivity()).get(OtherUserProfileViewModel.class);
        mMainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_other_profile, container, false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        setupViews();
        mModel.setNavController(mMainModel.getNavController());
        mModel.callRepositoryForUser(getArguments().getString(OTHER_USER_ID));
        mModel.getUserLiveData().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                ((MainActivity)getActivity()).getToolbar().setTitle(user.getName());
                fillUserData(user);
                mModel.setOtherUser(user);
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
            }
        });
    }

    private void setupViews() {
        Toolbar toolbar = ((MainActivity)getActivity()).getToolbar();
        toolbar.getMenu().clear();
        mNoPostsView = mBinding.getRoot().findViewById(R.id.noPostsLayout);
        mProgressBar = mBinding.getRoot().findViewById(R.id.progressBar);
        mProfilePicIW = mBinding.profilePic;
        mProgressBar.setVisibility(View.VISIBLE); // show loading animation when posts are being loaded
        mNamesTV = mBinding.getRoot().findViewById(R.id.namesTv);
        mEmailTV = mBinding.getRoot().findViewById(R.id.emailTv);
        mGoalsTV = mBinding.getRoot().findViewById(R.id.goalsInfoTv);
    }

    private void fillUserData(User user) {
        String names = user.getName() + " " + user.getLastname();
        mNamesTV.setText(names);
        mEmailTV.setText(user.getEmail());
        mGoalsTV.setText(Arrays.toString(user.getGoalTags()));
        setProfilePic(user);
    }

    private void setProfilePic(User user) {
        String imgUrl = user.getProfilePicUrl();
        if(imgUrl != null){
            if(!imgUrl.equals("")){
                Picasso.get()
                        .load(imgUrl)
                        .resize(300, 300)
                        .centerCrop()
                        .placeholder(R.drawable.progress_img_animation)
                        .into(mProfilePicIW);
            }
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

}
