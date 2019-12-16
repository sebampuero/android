package com.example.tm18app.network;

import com.example.tm18app.model.Goal;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * {@link retrofit2.Retrofit} Interface for Goals endpoints
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public interface GoalsRestInterface {

    @GET("api/goals/goals")
    Call<List<Goal>> getGoals();

    @POST("api/goals/goal")
    Call<Void> postGoalRequest(@Field("goals") String goals, @Field("userID") String userID);

}
