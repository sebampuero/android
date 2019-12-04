package com.example.tm18app.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tm18app.network.PostRestInterface;
import com.example.tm18app.network.RetrofitNetworkConnectionSingleton;
import com.example.tm18app.pojos.Comment;
import com.example.tm18app.pojos.Post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for {@link Post} and {@link Comment} responsible for managing the connection to the remote data source where
 * the model data is persisted. See <a href="https://developer.android.com/jetpack/docs/guide#overview">Jetpack architecture overview</a>
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class PostItemRepository {

    private PostRestInterface postRestInterface;

    public PostItemRepository(){
        postRestInterface = RetrofitNetworkConnectionSingleton.
                getInstance().retrofitInstance().create(PostRestInterface.class);
    }

    /**
     * Fetches a {@link List} of {@link Post} that correspond to a given goalId
     * @param goalIds {@link Integer} the goalId for the corresponding posts
     * @return {@link LiveData} containing the {@link List} of {@link Post} items
     */
    public LiveData<List<Post>> getPosts(List<String> goalIds) {
        final MutableLiveData<List<Post>> data = new MutableLiveData<>();
        postRestInterface.getPostsWithGoals(goalIds).enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if(response.body() != null){
                    data.setValue(response.body());
                    Log.e("TAG", "repo not null");
                }
                else{
                    Log.e("TAG", "repo null");
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {

            }
        });
        return data;
    }

    /**
     * Fetches a {@link List} of {@link Post} that correspond to a given userId
     * @param userId {@link Integer} the userId
     * @return {@link LiveData} containing the {@link List} of {@link com.example.tm18app.pojos.User} items
     */
    public LiveData<List<Post>> getUserPosts(String userId) {
        final MutableLiveData<List<Post>> data = new MutableLiveData<>();
        postRestInterface.getPostsByUserId(userId).enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if(response.body() != null){
                    data.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {

            }
        });
        return data;
    }

    /**
     * Fetches a {@link List} of {@link Comment} that correspond to a given postId
     * @param postID {@link Integer} the postId
     * @return {@link LiveData} containing the {@link List} of {@link Comment} items
     */
    public MutableLiveData<List<Comment>> getComments(String postID) {
        final MutableLiveData<List<Comment>> data = new MutableLiveData<>();
        postRestInterface.getCommentsByPostId(postID).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if(response.body() != null){
                    data.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {

            }
        });
        return data;
    }

    /**
     * Inserts a {@link Comment} by sending a POST Request to the API
     * @param comment {@link Comment} the comment to be inserted
     * @param commentLiveData {@link MutableLiveData} containing the {@link List} of {@link Comment}. The {@link com.example.tm18app.fragment.CommentSectionFragment} observes this parameter to display changes
     */
    public void createComment(final Comment comment, MutableLiveData<List<Comment>> commentLiveData) {
        final MutableLiveData<List<Comment>> data = commentLiveData;
        postRestInterface.newComment(comment).enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if(data.getValue() != null){
                    ArrayList<Comment> comments = new ArrayList<>(data.getValue());
                    comments.add(response.body());
                    data.setValue(comments);
                }else{
                    ArrayList<Comment> comments = new ArrayList<>();
                    comments.add(response.body());
                    data.setValue(comments);
                }
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {

            }
        });
    }

    /**
     * Inserts a {@link Post} by sending a POST Request to the API
     * @param post {@link Post} the post to be inserted
     * @param postLiveData {@link MutableLiveData} containing a {@link HashMap} of the {@link Integer} HTTP Status code and a {@link String} message.
     * The {@link com.example.tm18app.fragment.NewPostFragment} observes the parameter to display changes according to the received HTTP Status code
     */
    public void createPost(Post post, MutableLiveData<HashMap<Integer, String>> postLiveData){
        final MutableLiveData<HashMap<Integer, String>> responseCode = postLiveData;
        postRestInterface.newPost(post).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                HashMap<Integer, String> hashMap = new HashMap<>();
                if(response.code() == 500)
                    hashMap.put(response.code(), "Server error");
                else if(response.code() == 200)
                    hashMap.put(response.code(), "Ok");
                responseCode.setValue(hashMap);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }
}
