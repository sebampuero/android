package com.example.tm18app.network;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.navigation.NavController;

import com.example.tm18app.R;
import com.example.tm18app.pojos.Post;

import java.io.IOException;
import java.lang.ref.WeakReference;

import retrofit2.Call;
import retrofit2.Response;

public class PostAsyncTask extends AsyncTask<String, String, String> {

    private NavController navController;
    private int statusCode;
    private WeakReference<Context> appContext;
    private PostRestInterface restClient;

    public PostAsyncTask(NavController navController, Context appContext) {
        this.navController = navController;
        this.appContext = new WeakReference<>(appContext);
        RetrofitNetworkConnectionSingleton retrofitNetworkConnectionSingleton = RetrofitNetworkConnectionSingleton.getInstance();
        restClient = retrofitNetworkConnectionSingleton.retrofitInstance().create(PostRestInterface.class);
    }

    @Override
    protected String doInBackground(String... strings) {
        String title = strings[0];
        String content = strings[1];
        String userID = strings[2];
        String goalID = strings[3];
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setUserID(Integer.valueOf(userID));
        post.setGoalId(Integer.valueOf(goalID));
        Call<Void> call = restClient.newPost(post);
        try {
            Response<Void> response = call.execute();
            statusCode = response.code();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        if(statusCode == 200){
            Toast.makeText(appContext.get(), appContext.get().getString(R.string.post_successfully_created), Toast.LENGTH_SHORT).show();
            navController.navigate(R.id.action_newPostFragment_to_feedFragment);
        }
        else if(statusCode == 500) {
            Toast.makeText(appContext.get(), appContext.get().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
        }else
            Toast.makeText(appContext.get(), appContext.get().getString(R.string.no_int_connection), Toast.LENGTH_SHORT).show();
    }
}
