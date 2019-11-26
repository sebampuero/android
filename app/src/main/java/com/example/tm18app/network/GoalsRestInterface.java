package com.example.tm18app.network;

import com.example.tm18app.pojos.Goal;
import com.example.tm18app.pojos.GoalItemSelection;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface GoalsRestInterface {

    @GET("api/goals/goals")
    Call<List<Goal>> getGoals();

    @POST("api/goals/goal")
    Call<Void> postGoalRequest(@Field("goals") String goals, @Field("userID") String userID);

}
