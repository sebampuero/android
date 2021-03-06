package com.example.tm18app.repository;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tm18app.model.Comment;
import com.example.tm18app.model.Post;
import com.example.tm18app.network.PostRestInterface;
import com.example.tm18app.network.RetrofitNetworkConnectionSingleton;
import com.example.tm18app.service.UploadService;

import java.util.ArrayList;
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

    private PostRestInterface mPostRestInterface;
    private final String TAG = getClass().getSimpleName();

    public PostItemRepository(){
        mPostRestInterface = RetrofitNetworkConnectionSingleton.
                getInstance().retrofitInstance().create(PostRestInterface.class);
    }

    /**
     * Fetches a {@link List} of {@link Post} that correspond to a given goalId
     * @param goalIds {@link Integer} the goalId for the corresponding posts
     * @param pushyToken {@link String} unique token of the logged in user
     * @return {@link LiveData} containing the {@link List} of {@link Post} items
     */
    public LiveData<List<Post>> getPosts(List<String> goalIds, String pushyToken, String page) {
        final MutableLiveData<List<Post>> data = new MutableLiveData<>();
        mPostRestInterface.getPostsWithGoals(page, goalIds, pushyToken).enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if(response.body() != null){
                    data.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }

    /**
     * Retrieves the total number of pages for pagination
     * @param goals {@link List}
     * @param pushyToken {@link String}
     * @return {@link MutableLiveData} containing the total number of pages
     */
    public LiveData<Integer> getPagesNumberForPosts(List<String> goals, String pushyToken) {
        final MutableLiveData<Integer> data = new MutableLiveData<>();
        mPostRestInterface.getTotalPagesForPosts(goals, pushyToken).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
            }
        });
        return data;
    }

    /**
     * Fetches a {@link List} of {@link Post} that correspond to a given mUserID
     * @param userId {@link Integer} the mUserID
     * @param pushyToken {@link String} unique token of the logged in user
     * @return {@link LiveData} containing the {@link List} of {@link com.example.tm18app.model.User} items
     */
    public LiveData<List<Post>> getUserPosts(String userId, String pushyToken, String page) {
        final MutableLiveData<List<Post>> data = new MutableLiveData<>();
        mPostRestInterface.getPostsByUserId(userId, pushyToken, page).enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if(response.body() != null){
                    data.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }

    /**
     * Retrieves the total number of pages for pagination of user posts
     * @param userId {@link String}
     * @param pushyToken {@link String}
     * @return {@link MutableLiveData} containing the total number of pages
     */
    public LiveData<Integer> getPagesNumberForPosts(String userId, String pushyToken) {
        final MutableLiveData<Integer> data = new MutableLiveData<>();
        mPostRestInterface.getTotalPagesForPosts(userId, pushyToken).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
            }
        });
        return data;
    }

    /**
     * Fetches a {@link List} of {@link Comment} that correspond to a given postId
     * @param postID {@link Integer} the postId
     * @param pushyToken {@link String} unique token of the logged in user
     * @return {@link LiveData} containing the {@link List} of {@link Comment} items
     */
    public MutableLiveData<List<Comment>> getComments(String postID, String pushyToken) {
        final MutableLiveData<List<Comment>> data = new MutableLiveData<>();
        mPostRestInterface.getCommentsByPostId(postID, pushyToken).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if(response.body() != null){
                    data.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }

    /**
     * Inserts a {@link Comment} by sending a POST Request to the API
     * @param comment {@link Comment} the comment to be inserted
     * @param commentLiveData {@link MutableLiveData} containing the {@link List} of {@link Comment}. The {@link com.example.tm18app.fragment.CommentSectionFragment} observes this parameter to display changes
     * @param pushyToken {@link String} unique token of the logged in user
     */
    public void createComment(final Comment comment,
                              MutableLiveData<List<Comment>> commentLiveData,
                              String pushyToken) {
        final MutableLiveData<List<Comment>> data = commentLiveData;
        mPostRestInterface.newComment(comment, pushyToken).enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                ArrayList<Comment> comments = new ArrayList<>();
                comments.add(response.body());
                data.setValue(comments);
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                data.setValue(null);
                Log.e(TAG, t.getMessage());
            }
        });
    }

    /**
     * Creates a {@link Post}. For the upload of the post, a service is used.
     * @see UploadService
     * @param post {@link Post} object containing post's data
     * @param pushyToken {@link String} unique token of the logged in user
     * @param context {@link Context}
     */
    public void createPost(Post post, String pushyToken, Context context){
        Intent serviceIntent = new Intent(context, UploadService.class);
        serviceIntent.putExtra("pushy", pushyToken);
        serviceIntent.putExtra("mTitle", post.getTitle());
        serviceIntent.putExtra("mContent", post.getContent());
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
     * @param pushyToken {@link String} unique token of the logged in user
     */
    public void deletePost(int postID, final MutableLiveData<Integer> statusCode, String pushyToken){
        mPostRestInterface.deletePost(String.valueOf(postID), pushyToken).enqueue(new Callback<Void>() {
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
     * @param pushyToken {@link String} unique token of the logged in user
     */
    public void deleteComment(int commentID, String pushyToken) {
        mPostRestInterface.deleteComment(String.valueOf(commentID), pushyToken).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }

    /**
     * Deletes a subscription to a given {@link Post}
     * @param userID {@link String} the id of the user to delete the subscription
     * @param postID {@link String} id of the post to delete the subscription from
     * @param pushyToken {@link String} unique token of the logged in user
     */
    public void deleteSubscription(String userID, String postID, String pushyToken) {
        mPostRestInterface.deletePostSubscription(userID, postID, pushyToken).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }

}
