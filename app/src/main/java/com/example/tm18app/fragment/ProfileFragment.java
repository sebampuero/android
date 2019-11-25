package com.example.tm18app.fragment;


import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.tm18app.R;
import com.example.tm18app.adapters.PostItemAdapter;
import com.example.tm18app.databinding.FragmentProfileBinding;
import com.example.tm18app.pojos.Post;
import com.example.tm18app.viewModels.MyViewModel;
import com.example.tm18app.viewModels.ProfileViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {


    private MyViewModel mainModel;
    private ProfileViewModel model;
    private FragmentProfileBinding binding;
    private RecyclerView recyclerView;
    private PostItemAdapter adapter;
    private List<Post> postsModelLists = new ArrayList<>();
    private ProgressBar progressBar;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        model = ViewModelProviders.of(getActivity()).get(ProfileViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false);
        binding.setMyVM(model);
        binding.setLifecycleOwner(this);
        model.setNavController(mainModel.getNavController());
        model.setContext(getActivity());
        progressBar = binding.progressBar;
        progressBar.setVisibility(View.VISIBLE);
        setupRecyclerView();
        fetchData();
        return binding.getRoot();
    }


    private void setupRecyclerView() {
        recyclerView = binding.goalsUserRv;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PostItemAdapter(getActivity(), (ArrayList<Post>) postsModelLists,
                mainModel.getNavController(), this);
        recyclerView.setAdapter(adapter);
    }

    private void fetchData() {
        model.getPostLiveData().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> posts) {
                if(posts != null){
                    postsModelLists.clear();
                    postsModelLists.addAll(posts);
                    Collections.shuffle(postsModelLists);
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }


}
