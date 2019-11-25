package com.example.tm18app.network;

import android.os.AsyncTask;

import androidx.lifecycle.MutableLiveData;

import com.example.tm18app.pojos.Comment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class PostCommentAsyncTask extends AsyncTask<String, String, Comment> {

    private int statusCode;
    private PostRestInterface postRestInterface;
    private MutableLiveData<List<Comment>> liveData;

    public PostCommentAsyncTask(MutableLiveData<List<Comment>> liveData) {
        statusCode = 0;
        RetrofitNetworkConnectionSingleton retrofitNetworkConnectionSingleton
                = RetrofitNetworkConnectionSingleton.getInstance();
        postRestInterface = retrofitNetworkConnectionSingleton.retrofitInstance()
                .create(PostRestInterface.class);
        this.liveData = liveData;
    }

    @Override
    protected Comment doInBackground(String... strings) {
        Comment comment = new Comment();
        comment.setContent(strings[0]);
        comment.setUserID(Integer.valueOf(strings[1]));
        comment.setPostID(Integer.valueOf(strings[2]));
        Call<Comment> call = postRestInterface.newComment(comment);
        Response<Comment> response;
        try {
            response = call.execute();
            statusCode = response.code();
            if(statusCode == 500)
                return null;
            else if(statusCode == 200)
                return response.body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Comment c) {
        if(liveData.getValue() != null){
            ArrayList<Comment> comments = new ArrayList<>(liveData.getValue());
            comments.add(c);
            liveData.setValue(comments);
        }
    }
}
