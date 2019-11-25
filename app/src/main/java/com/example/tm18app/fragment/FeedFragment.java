package com.example.tm18app.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.R;
import com.example.tm18app.adapters.PostItemAdapter;
import com.example.tm18app.databinding.FragmentFeedBinding;
import com.example.tm18app.pojos.Post;
import com.example.tm18app.viewModels.FeedViewModel;
import com.example.tm18app.viewModels.MyViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment {

    private MyViewModel mainModel;
    private FeedViewModel model;
    private FragmentFeedBinding binding;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private PostItemAdapter adapter;
    private List<Post> postsModelLists = new ArrayList<>();
    private ProgressBar progressBar;

    public FeedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        model = ViewModelProviders.of(getActivity()).get(FeedViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feed, container, false);
        binding.setMyVM(model);
        binding.setLifecycleOwner(this);
        progressBar = binding.pogressBar;
        progressBar.setVisibility(View.VISIBLE);
        model.setNavController(mainModel.getNavController());
        setupSwipeRefreshLayout();
        setupRecyclerView();
        checkIfGoalsExist();
        model.setContext(getActivity());
        fetchData();
        return binding.getRoot();
    }

    private void checkIfGoalsExist() {
        SharedPreferences preferences = getActivity().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        if(preferences.getString(Constant.GOAL_TAGS, null) == null){
            Snackbar.make(binding.getRoot(), getActivity().getString(R.string.no_goals_msg), Snackbar.LENGTH_LONG).show();
        }
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout = binding.swipeRefreshLayout;
        final Fragment fragment = this;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                model.getPostLiveData().removeObservers(fragment);
                fetchData();
                model.fetchData();
            }
        });
    }

    private void fetchData() {
        model.getPostLiveData().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> posts) {
                if(posts != null){
                    postsModelLists.clear();
                    postsModelLists.addAll(posts);
                    Collections.sort(postsModelLists);
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView = binding.rvFeed;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PostItemAdapter(getActivity(), (ArrayList<Post>) postsModelLists,
                mainModel.getNavController(), this);
        recyclerView.setAdapter(adapter);
    }


}
