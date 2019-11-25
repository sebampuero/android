package com.example.tm18app.network;

import com.example.tm18app.pojos.PasswordReset;
import com.example.tm18app.pojos.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface UserRestInterface {

    @POST("api/goals/login")
    Call<User> loginUser(@Body User user);

    @POST("api/goals/register")
    Call<User> registerUser(@Body User user);

    @POST("api/goals/update")
    Call<User> updateUser(@Body User user);

    @POST("api/goals/update/password")
    Call<Void> updatePassword(@Body PasswordReset passwordReset);
}
