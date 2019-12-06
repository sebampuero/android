package com.example.tm18app.fragment;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.R;
import com.example.tm18app.adapters.PostItemAdapter;
import com.example.tm18app.databinding.FragmentFeedBinding;
import com.example.tm18app.pojos.Post;
import com.example.tm18app.viewModels.FeedViewModel;
import com.example.tm18app.viewModels.MyViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.pushy.sdk.Pushy;
import me.pushy.sdk.config.PushyPreferenceKeys;
import me.pushy.sdk.config.PushySDK;
import me.pushy.sdk.model.PushyDeviceCredentials;
import me.pushy.sdk.util.PushyAuthentication;


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
        if(goalsExist){
            model.fetchData();
            fetchData();
        }else{
            progressBar.setVisibility(View.GONE);
        }
        return binding.getRoot();
    }

    //TODO: take to login fragment and register fragment. Login fragment fetches token from DB and Registerfragment creates new one
    // save token and auth key to db when registering and fetch both when login , create PushyCredentials object
    // and use Pushy.setCredentials()
    private void handlePushyServices() {
        if (!Pushy.isRegistered(getActivity().getApplicationContext())) {
            new RegisterForPushNotificationsAsync(getActivity().getApplicationContext()).execute();
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

    private static class RegisterForPushNotificationsAsync extends AsyncTask<Void, Void, Exception> {

        WeakReference<Context> appContext;

        RegisterForPushNotificationsAsync(Context applicationContext) {
            this.appContext = new WeakReference<>(applicationContext);
        }

        protected Exception doInBackground(Void... params) {
            try {
                // Assign a unique token to this device
                String deviceToken = Pushy.register(appContext.get());
                // Log it for debugging purposes
                Log.e("TAG", "Pushy device token: " + deviceToken);
                Log.e("TAG", "Pushy auth key: " + Pushy.getDeviceCredentials(appContext.get()).authKey);
            }
            catch (Exception exc) {
                // Return exc to onPostExecute
                return exc;
            }

            // Success
            return null;
        }

        @Override
        protected void onPostExecute(Exception exc) {
            // Failed?
            if (exc != null) {
                // Show error as toast message
                Toast.makeText(appContext.get(), exc.toString(), Toast.LENGTH_LONG).show();
            }
            // Succeeded, optionally do something to alert the user
        }
    }
}
