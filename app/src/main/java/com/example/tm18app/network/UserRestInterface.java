package com.example.tm18app.network;

import com.example.tm18app.model.PasswordReset;
import com.example.tm18app.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
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

    @POST("api/users/login")
    Call<User> loginUser(@Body User user);

    @POST("api/users/register")
    Call<User> registerUser(@Body User user);

    @POST("api/users/update")
    Call<User> updateUser(@Body User user, @Header("pushy") String pushyToken);

    @POST("api/users/update/password")
    Call<Void> updatePassword(@Body PasswordReset passwordReset, @Header("pushy") String pushyToken);

    @GET("api/users/{userId}")
    Call<User> getUserById(@Path("userId") String userId, @Header("pushy") String pushyToken);
}
