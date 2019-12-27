package com.example.tm18app.repository;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;
import com.example.tm18app.network.PostRestInterface;
import com.example.tm18app.network.RetrofitNetworkConnectionSingleton;
import com.example.tm18app.model.Comment;
import com.example.tm18app.model.Post;
import com.example.tm18app.service.UploadService;

import org.json.HTTP;

import java.io.IOException;
import java.net.HttpURLConnection;
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
    public LiveData<List<Post>> getPosts(List<String> goalIds, String pushyToken, String page) {
        final MutableLiveData<List<Post>> data = new MutableLiveData<>();
        postRestInterface.getPostsWithGoals(page, goalIds, pushyToken).enqueue(new Callback<List<Post>>() {
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
     * Fetches a {@link List} of {@link Post} that correspond to a given userId
     * @param userId {@link Integer} the userId
     * @return {@link LiveData} containing the {@link List} of {@link com.example.tm18app.model.User} items
     */
    public LiveData<List<Post>> getUserPosts(String userId, String pushyToken, String page) {
        final MutableLiveData<List<Post>> data = new MutableLiveData<>();
        postRestInterface.getPostsByUserId(userId, pushyToken, page).enqueue(new Callback<List<Post>>() {
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
    public MutableLiveData<List<Comment>> getComments(String postID, String pushyToken) {
        final MutableLiveData<List<Comment>> data = new MutableLiveData<>();
        postRestInterface.getCommentsByPostId(postID, pushyToken).enqueue(new Callback<List<Comment>>() {
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
    public void createComment(final Comment comment,
                              MutableLiveData<List<Comment>> commentLiveData,
                              String pushyToken) {
        final MutableLiveData<List<Comment>> data = commentLiveData;
        postRestInterface.newComment(comment, pushyToken).enqueue(new Callback<Comment>() {
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

    public void createPost(Post post, String pushyToken, Context context){
        Intent serviceIntent = new Intent(context, UploadService.class);
        serviceIntent.putExtra("pushy", pushyToken);
        serviceIntent.putExtra("title", post.getTitle());
        serviceIntent.putExtra("content", post.getContent());
        serviceIntent.putExtra("imageUri", post.getContentImageURI());
        serviceIntent.putExtra("videoUri", post.getContentVideoURI());
        serviceIntent.putExtra("userID",  post.getUserID());
        serviceIntent.putExtra("goalID", post.getGoalId());
        context.startService(serviceIntent);
    }

    /**
     * Deletes a {@link Post} from the server.
     * @param postID {@link Integer} the post id of the post to be deleted
     * @param statusCode {@link MutableLiveData} containing the response status code
     */
    public void deletePost(int postID, final MutableLiveData<Integer> statusCode, String pushyToken){
        postRestInterface.deletePost(String.valueOf(postID), pushyToken).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                statusCode.setValue(response.code());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }

    /**
     * Deletes a {@link Comment} from the server.
     * @param commentID {@link Integer} the comment id of the comment to be deleted
     */
    public void deleteComment(int commentID, String pushyToken) {
        postRestInterface.deleteComment(String.valueOf(commentID), pushyToken).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }

    public void deleteSubscription(String userID, String postID, String pushyToken) {
        postRestInterface.deletePostSubscription(userID, postID, pushyToken).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }
}
