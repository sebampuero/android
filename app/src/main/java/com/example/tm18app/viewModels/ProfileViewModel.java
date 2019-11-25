package com.example.tm18app.viewModels;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.R;
import com.example.tm18app.pojos.Post;
import com.example.tm18app.repository.PostItemRepository;

import java.util.List;

public class ProfileViewModel extends ViewModel {

    public MutableLiveData<String> names = new MutableLiveData<>();
    public MutableLiveData<String> email = new MutableLiveData<>();
    public MutableLiveData<String> goalsList = new MutableLiveData<>();

    private NavController navController;
    private Context appContext;

    private PostItemRepository postItemRepository;
    private LiveData<List<Post>> postLiveData;

    public void setNavController(NavController navController) {
        this.navController = navController;
    }

    public LiveData<List<Post>> getPostLiveData() {
        return postLiveData;
    }

    public void setContext(FragmentActivity activity) {
        this.appContext = activity.getApplicationContext();
        fetchData();
        populateInfo();
    }

    private void populateInfo() {
        SharedPreferences preferences = appContext
                .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        String name = preferences.getString(Constant.NAME, null);
        String lastname = preferences.getString(Constant.LASTNAME, null);
        names.setValue(name + " " + lastname);
        email.setValue(preferences.getString(Constant.EMAIL, null));
        goalsList.setValue(preferences.getString(Constant.GOAL_TAGS, null));
    }

    private void fetchData() {
        postItemRepository = new PostItemRepository();
        SharedPreferences preferences = appContext
                .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        String userId = String.valueOf(preferences.getInt(Constant.USER_ID, 0));
        this.postLiveData = postItemRepository.getUserPosts(userId);
    }

    public void onEditInfoClicked() {
        navController.navigate(R.id.action_profileFragment_to_editProfileFragment);
    }
}

