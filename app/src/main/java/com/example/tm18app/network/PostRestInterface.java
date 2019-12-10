package com.example.tm18app.network;

import com.example.tm18app.pojos.Comment;
import com.example.tm18app.pojos.Post;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * {@link retrofit2.Retrofit} Interface for Post endpoints
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public interface PostRestInterface {

    @POST("api/goals/post")
    Call<Void> newPost(@Body Post post);

    @GET("api/goals/posts/{goalId}")
    Call<List<Post>> getPostsByGoalId(@Path("goalId") String goalId);

    @POST("api/goals/posts")
    Call<List<Post>> getPostsWithGoals(@Body List<String> goals);

    @GET("api/goals/posts/user/{userId}")
    Call<List<Post>> getPostsByUserId(@Path("userId") String userId);

    @POST("api/goals/comment")
    Call<Comment> newComment(@Body Comment comment);

    @GET("api/goals/comments/{postId}")
    Call<List<Comment>> getCommentsByPostId(@Path("postId") String postId);

    @DELETE("api/goals/post/{postId}")
    Call<Void> deletePost(@Path("postId") String postId);

    @DELETE("api/goals/comment/{commentId}")
    Call<Void> deleteComment(@Path("commentId") String commentId);
}
