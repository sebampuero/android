package com.example.tm18app.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.pushy.sdk.Pushy;


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
    private boolean goalsExist = true;

    private LinearLayout feedLinearLayout;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        checkBackFromMainFragment();
        mainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        model = ViewModelProviders.of(getActivity()).get(FeedViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feed, container, false);
        binding.setMyVM(model);
        binding.setLifecycleOwner(this);
        progressBar = binding.progressBarFeed;
        feedLinearLayout = binding.feedLinearLayout;
        progressBar.setVisibility(View.VISIBLE);
        model.setNavController(mainModel.getNavController());
        setupSwipeRefreshLayout();
        setupRecyclerView();
        model.setContext(getActivity());
        checkIfGoalsExist();
        requestPushyCreds();
        if(goalsExist){
            model.fetchData();
            fetchData();
        }else{
            progressBar.setVisibility(View.GONE);
        }
        return binding.getRoot();
    }

    private void checkBackFromMainFragment() {
        SharedPreferences preferences = getActivity().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        if(!preferences.getBoolean(Constant.LOGGED_IN, false)){
            getActivity().finish();
        }
    }

    private void requestPushyCreds() {
        if (!Pushy.isRegistered(getActivity().getApplicationContext())) {
            SharedPreferences preferences = getActivity().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
            int userID = preferences.getInt(Constant.USER_ID, 0);
            new UserPushyTokenAsyncTask(getActivity().getApplicationContext()).execute(Constant.PUSHY_CREDS_ENDPOINT + userID);
        }
    }

    private void checkIfGoalsExist() {
        SharedPreferences preferences = getActivity().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        if(preferences.getString(Constant.GOAL_TAGS, null) == null){
            Snackbar.make(binding.getRoot(), getActivity().getString(R.string.no_goals_msg), Snackbar.LENGTH_LONG).show();
            feedLinearLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            postsModelLists.clear();
            goalsExist = false;
        }
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout = binding.swipeRefreshLayout;
        final Fragment fragment = this;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(model.getPostLiveData() != null && goalsExist){
                    model.getPostLiveData().removeObservers(fragment);
                    fetchData();
                    model.fetchData();
                }else{
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private void fetchData() {
        if(model.getPostLiveData() != null){
            model.getPostLiveData().observe(this, new Observer<List<Post>>() {
                @Override
                public void onChanged(List<Post> posts) {
                    if(posts.size() > 0){
                        if(goalsExist){
                            postsModelLists.clear();
                            postsModelLists.addAll(posts);
                            Collections.sort(postsModelLists);
                            adapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                            feedLinearLayout.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }else{
                        feedLinearLayout.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }else{
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        recyclerView = binding.rvFeed;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PostItemAdapter((ArrayList<Post>) postsModelLists,
                mainModel.getNavController(), this);
        recyclerView.setAdapter(adapter);
    }
}
