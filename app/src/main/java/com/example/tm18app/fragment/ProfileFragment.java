package com.example.tm18app.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
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
import android.widget.Toast;

import com.example.tm18app.R;
import com.example.tm18app.adapters.PostItemAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.FragmentProfileBinding;
import com.example.tm18app.pojos.Post;
import com.example.tm18app.viewModels.CurrentProfileViewModel;
import com.example.tm18app.viewModels.MyViewModel;
import com.example.tm18app.viewModels.ProfileViewModel;
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

    private MyViewModel mainModel;
    private CurrentProfileViewModel model;
    private FragmentProfileBinding binding;
    private RecyclerView recyclerView;
    private PostItemAdapter adapter;
    private List<Post> postsModelLists = new ArrayList<>();
    private ProgressBar progressBar;
    private LinearLayout noPostsLayout;
    private ImageView profilePic;
    private SharedPreferences prefs;
    private TextView namesTv;
    private TextView emailTv;
    private TextView goalsTv;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        model = ViewModelProviders.of(getActivity()).get(CurrentProfileViewModel.class);
        prefs = getContext().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        mainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false);
        binding.setMyVM(model);
        binding.setLifecycleOwner(this);
        model.setNavController(mainModel.getNavController());
        setupViews();
        fillUserData();
        model.setPrefs(prefs);
        model.callRepositoryForPosts();
        setupRecyclerView();
        fetchData();
        return binding.getRoot();
    }

    private void fillUserData() {
        String names = prefs
                .getString(Constant.NAME,"") + " " + prefs.getString(Constant.LASTNAME, "");
        String email = prefs.getString(Constant.EMAIL, "");
        String goals = prefs.getString(Constant.GOAL_TAGS, "");
        namesTv.setText(names);
        emailTv.setText(email);
        goalsTv.setText(goals);
    }


    private void setupViews() {
        noPostsLayout = binding.noPostsLayout;
        progressBar = binding.progressBar;
        profilePic = binding.profilePic;
        progressBar.setVisibility(View.VISIBLE); // show loading animation when posts are being loaded
        namesTv = binding.namesTv;
        emailTv = binding.emailTv;
        goalsTv = binding.goalsInfoTv;
    }

    /**
     * Sets up the {@link RecyclerView} for the user's Posts list in the profile
     */
    private void setupRecyclerView() {
        recyclerView = binding.goalsUserRv;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PostItemAdapter((ArrayList<Post>) postsModelLists,
                mainModel.getNavController(), this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Fetches the {@link List} of {@link Post} items from the server.
     */
    private void fetchData() {
        model.getPostLiveData().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> posts) {
                if(posts != null){
                    if(posts.size() > 0){
                        postsModelLists.clear();
                        postsModelLists.addAll(posts);
                        Collections.sort(postsModelLists);
                        adapter.notifyDataSetChanged();
                        recyclerView.setVisibility(View.VISIBLE);
                    }else{
                        noPostsLayout.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        setProfilePic();
    }

    private void setProfilePic() {
        String imgUrl = prefs.getString(Constant.PROFILE_PIC_URL, null);
        if(imgUrl != null){
            if(!imgUrl.equals("")){
                Picasso.get().load(prefs.getString(Constant.PROFILE_PIC_URL, null))
                        .resize(300, 300).centerCrop().into(profilePic);
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
