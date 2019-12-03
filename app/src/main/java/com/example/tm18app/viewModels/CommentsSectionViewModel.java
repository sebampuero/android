package com.example.tm18app.viewModels;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.network.PostCommentAsyncTask;
import com.example.tm18app.network.PostRestInterface;
import com.example.tm18app.network.RetrofitNetworkConnectionSingleton;
import com.example.tm18app.pojos.Comment;
import com.example.tm18app.repository.PostItemRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class CommentsSectionViewModel extends ViewModel {

    public MutableLiveData<String> inputComment = new MutableLiveData<>();

    private Context appContext;
    private String postID;
    private MutableLiveData<List<Comment>> commentLiveData;

    public MutableLiveData<List<Comment>> getCommentLiveData() {
        return commentLiveData;
    }


    public void onPostComment() {
        if(inputComment.getValue() != null){
            if(!inputComment.getValue().trim().equals("")){
                SharedPreferences preferences = appContext
                        .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
                PostItemRepository postItemRepository = new PostItemRepository();
                Comment comment = new Comment();
                comment.setContent(inputComment.getValue());
                comment.setPostID(Integer.parseInt(postID));
                comment.setUserID(preferences.getInt(Constant.USER_ID, 0));
                postItemRepository.createComment(comment, commentLiveData);
                inputComment.setValue("");
            }
        }
    }

    public void setAppContext(Context appContext) {
        this.appContext = appContext;
        fetchData();
    }

    private void fetchData() {
        PostItemRepository postItemRepository = new PostItemRepository();
        this.commentLiveData = postItemRepository.getComments(postID);
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

}
