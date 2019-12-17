package com.example.tm18app.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;
import com.example.tm18app.adapters.PostItemAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.FragmentProfileBinding;
import com.example.tm18app.model.Post;
import com.example.tm18app.viewModels.CurrentProfileViewModel;
import com.example.tm18app.viewModels.MyViewModel;
import com.squareup.picasso.Picasso;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass. Responsible for UI and events for the profile UI.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class ProfileFragment extends Fragment implements PostItemAdapter.OnPostDeleteListener{

    private MyViewModel mMainModel;
    private CurrentProfileViewModel mModel;
    private FragmentProfileBinding mBinding;
    private RecyclerView mRecyclerView;
    private PostItemAdapter mAdapter;
    private List<Post> mPostsList = new ArrayList<>();
    private ProgressBar mProgressBar;
    private LinearLayout mNoPostsView;
    private ImageView mProfilePicIW;
    private SharedPreferences mPrefs;
    private TextView mNamesTV;
    private TextView mEmailTV;
    private TextView mGoalsTV;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mModel = ViewModelProviders.of(getActivity()).get(CurrentProfileViewModel.class);
        mPrefs = getContext().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        mMainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        mModel.setNavController(mMainModel.getNavController());
        setupViews();
        fillUserData();
        mModel.setPrefs(mPrefs);
        mModel.callRepositoryForPosts();
        setupRecyclerView();
        fetchData();
        return mBinding.getRoot();
    }

    private void fillUserData() {
        String names = mPrefs
                .getString(Constant.NAME,"") + " " + mPrefs.getString(Constant.LASTNAME, "");
        String email = mPrefs.getString(Constant.EMAIL, "");
        String goals = mPrefs.getString(Constant.GOAL_TAGS, "");
        mNamesTV.setText(names);
        mEmailTV.setText(email);
        mGoalsTV.setText(goals);
    }


    private void setupViews() {
        Toolbar toolbar = ((MainActivity)getActivity()).getToolbar();
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.profile_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.chatsIcon)
                    mMainModel.getNavController().navigate(R.id.action_profileFragment_to_chatsFragment);
                return false;
            }
        });
        //mToolbar.setTitle(getResources().getString(R.string.profile_toolbar_title));
        mNoPostsView = mBinding.getRoot().findViewById(R.id.noPostsLayout);
        mProgressBar = mBinding.getRoot().findViewById(R.id.progressBar);
        mProfilePicIW = mBinding.profilePic;
        mProgressBar.setVisibility(View.VISIBLE); // show loading animation when posts are being loaded
        mNamesTV = mBinding.getRoot().findViewById(R.id.namesTv);
        mEmailTV = mBinding.getRoot().findViewById(R.id.emailTv);
        mGoalsTV = mBinding.getRoot().findViewById(R.id.goalsInfoTv);
    }

    /**
     * Sets up the {@link RecyclerView} for the user's Posts list in the profile
     */
    private void setupRecyclerView() {
        mRecyclerView = mBinding.getRoot().findViewById(R.id.goalsUserRv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new PostItemAdapter((ArrayList<Post>) mPostsList,
                mMainModel.getNavController(), this);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Fetches the {@link List} of {@link Post} items from the server.
     */
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
        setProfilePic();
    }

    private void setProfilePic() {
        String imgUrl = mPrefs.getString(Constant.PROFILE_PIC_URL, null);
        if(imgUrl != null){
            if(!imgUrl.equals("")){
                Picasso.get()
                        .load(mPrefs.getString(Constant.PROFILE_PIC_URL, null))
                        .resize(300, 300)
                        .centerCrop()
                        .placeholder(R.drawable.progress_img_animation)
                        .into(mProfilePicIW);
            }
        }
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
    }
}
