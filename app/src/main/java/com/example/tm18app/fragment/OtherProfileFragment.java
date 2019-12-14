package com.example.tm18app.fragment;


import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
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
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.FragmentOtherProfileBinding;
import com.example.tm18app.pojos.Post;
import com.example.tm18app.pojos.User;
import com.example.tm18app.viewModels.MyViewModel;
import com.example.tm18app.viewModels.OtherUserProfileViewModel;
import com.example.tm18app.viewModels.ProfileViewModel;
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

    private MyViewModel mainModel;
    private OtherUserProfileViewModel model;
    private FragmentOtherProfileBinding binding;
    private RecyclerView recyclerView;
    private PostItemAdapter adapter;
    private List<Post> postsModelLists = new ArrayList<>();
    private ProgressBar progressBar;
    private LinearLayout noPostsLayout;
    private ImageView profilePic;
    private TextView namesTv;
    private TextView emailTv;
    private TextView goalsTv;

    public OtherProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        model = ViewModelProviders.of(getActivity()).get(OtherUserProfileViewModel.class);
        mainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_other_profile, container, false);
        binding.setMyVM(model);
        binding.setLifecycleOwner(this);
        setupViews();
        model.setNavController(mainModel.getNavController());
        model.callRepositoryForUser(getArguments().getString(OTHER_USER_ID));
        model.getUserLiveData().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                ((MainActivity)getActivity()).getToolbar().setTitle(user.getName());
                fillUserData(user);
                model.setOtherUser(user);
                model.callRepositoryForPosts();
                fetchData();
            }
        });
        setupRecyclerView();
        return binding.getRoot();
    }

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

    private void fillUserData(User user) {
        String names = user.getName() + " " + user.getLastname();
        namesTv.setText(names);
        emailTv.setText(user.getEmail());
        goalsTv.setText(Arrays.toString(user.getGoalTags()));
        setProfilePic(user);
    }

    private void setProfilePic(User user) {
        String imgUrl = user.getProfilePicUrl();
        if(imgUrl != null){
            if(!imgUrl.equals("")){
                Picasso.get().load(imgUrl)
                        .resize(300, 300).centerCrop().into(profilePic);
            }
        }
    }

    private void setupRecyclerView() {
        recyclerView = binding.goalsUserRv;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PostItemAdapter((ArrayList<Post>) postsModelLists,
                mainModel.getNavController(), this);
        recyclerView.setAdapter(adapter);
    }

}
