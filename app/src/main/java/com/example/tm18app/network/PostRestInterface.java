package com.example.tm18app.network;

import com.example.tm18app.model.Comment;
import com.example.tm18app.model.Post;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * {@link retrofit2.Retrofit} Interface for Post endpoints
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public interface PostRestInterface {

    @POST("api/posts/")
    Call<Void> newPost(@Body Post post,
                       @Header("pushy") String pushyToken);

    @POST("api/posts/goals/{page}")
    Call<List<Post>> getPostsWithGoals(@Path("page") String page, @Body List<String> goals,
                                       @Header("pushy") String pushyToken);
    @POST("api/posts/goals/pagination/totalPages")
    Call<Integer> getTotalPagesForPosts(@Body List<String> goals, @Header("pushy") String pushyToken);

    @GET("api/posts/user/{mUserID}/{page}")
    Call<List<Post>> getPostsByUserId(@Path("mUserID") String userId, @Path("page") String page,
                                      @Header("pushy") String pushyToken);

    @GET("api/posts/user/pagination/totalPages/{mUserID}")
    Call<Integer> getTotalPagesForPosts(@Path("mUserID") String userId, @Header("pushy") String pushyToken);

    @POST("api/posts/comment")
    Call<Comment> newComment(@Body Comment comment,
                             @Header("pushy") String pushyToken);

    @GET("api/posts/comments/{postId}")
    Call<List<Comment>> getCommentsByPostId(@Path("postId") String postId,
                                            @Header("pushy") String pushyToken);

    @DELETE("api/posts/{postId}")
    Call<Void> deletePost(@Path("postId") String postId,
                          @Header("pushy") String pushyToken);

    @DELETE("api/posts/comment/{commentId}")
    Call<Void> deleteComment(@Path("commentId") String commentId,
                             @Header("pushy") String pushyToken);

    @POST("api/posts/subscription")
    Call<Void> deletePostSubscription(@Query("userID")String userID,
                                      @Query("postID") String postID,
                                      @Header("pushy") String pushyToken);
}
