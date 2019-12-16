package com.example.tm18app.network;

import com.example.tm18app.model.PasswordReset;
import com.example.tm18app.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * {@link retrofit2.Retrofit} Interface for User endpoints
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public interface UserRestInterface {

    @POST("api/goals/login")
    Call<User> loginUser(@Body User user);

    @POST("api/goals/register")
    Call<User> registerUser(@Body User user);

    @POST("api/goals/update")
    Call<User> updateUser(@Body User user);

    @POST("api/goals/update/password")
    Call<Void> updatePassword(@Body PasswordReset passwordReset);

    @GET("api/goals/user/{userId}")
    Call<User> getUserById(@Path("userId") String userId);
}
