package com.example.tm18app.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
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
 * A simple {@link Fragment} subclass. Responsible for UI and events for the feed section UI.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class FeedFragment extends Fragment implements OnPostDeleteListener{

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
        checkBackBtnPressedFromMainFragment();
        mainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        model = ViewModelProviders.of(getActivity()).get(FeedViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feed, container, false);
        binding.setMyVM(model);
        binding.setLifecycleOwner(this);
        progressBar = binding.progressBarFeed;
        feedLinearLayout = binding.feedLinearLayout;
        progressBar.setVisibility(View.VISIBLE); // to show that posts are loading
        model.setNavController(mainModel.getNavController());
        setupSwipeRefreshLayout(); // swipe refresh for the possibility to reload posts
        setupRecyclerView();
        model.setContext(getContext());
        checkIfGoalsExist();
        requestPushyCreds();
        if(goalsExist){ // if user has selected goals, fetch posts
            model.callRepository();
            fetchData();
        }else{
            progressBar.setVisibility(View.GONE);
        }
        return binding.getRoot();
    }

    /**
     * Temporary method to prevent going back to the {@link FeedFragment} when the user logs out.
     * This is  a workaround and wouldn't be propery to keep on production
     */
    private void checkBackBtnPressedFromMainFragment() {
        //TODO: Find alternative
        SharedPreferences preferences = getActivity().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        if(!preferences.getBoolean(Constant.LOGGED_IN, false)){
            getActivity().finish(); // closes the activity when the app detects the user swipes back
            // when logged out
        }
    }

    /**
     * Requests the user's {@link me.pushy.sdk.model.PushyDeviceCredentials} from the database to
     * later store them on the device's internal storage.
     * @see me.pushy.sdk.util.PushyPersistence
     */
    private void requestPushyCreds() {
        // Only requests pushy creds then there are no credentials stored on the device.
        // Do not waste network resources
        if (!Pushy.isRegistered(getActivity().getApplicationContext())) {
            SharedPreferences preferences = getActivity().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
            int userID = preferences.getInt(Constant.USER_ID, 0);
            new UserPushyTokenAsyncTask(getActivity().getApplicationContext()).execute(Constant.PUSHY_CREDS_ENDPOINT + userID);
        }
    }

    /**
     * Checks if the user has selected goals. If otherwise, a {@link Snackbar} is shown explaining
     * that goals can be selected on the Profile.
     */
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

    /**
     * Sets up the {@link SwipeRefreshLayout} for the Feed UI.
     */
    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout = binding.swipeRefreshLayout;
        final Fragment fragment = this;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(model.getPostLiveData() != null && goalsExist){
                    model.getPostLiveData().removeObservers(fragment);
                    fetchData();
                    model.callRepository();
                }else{
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    /**
     * Sets up Observer to observe changes on the {@link androidx.lifecycle.LiveData} containing
     * a {@link List} of Posts
     */
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

    /**
     * Sets up the {@link RecyclerView} for the Feed UI.
     */
    private void setupRecyclerView() {
        recyclerView = binding.rvFeed;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PostItemAdapter((ArrayList<Post>) postsModelLists,
                mainModel.getNavController(), this);
        recyclerView.setAdapter(adapter);
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
        if(statusCode == 500){
            Toast.makeText(getContext(), getContext().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
        }else if(statusCode == 200){
            Toast.makeText(getContext(), "Post deleted", Toast.LENGTH_SHORT).show();
        }
    }
}
