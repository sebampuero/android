package com.example.tm18app.network;

import com.example.tm18app.model.Goal;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * {@link retrofit2.Retrofit} Interface for Goals endpoints
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public interface GoalsRestInterface {

    @GET("api/goals")
    Call<List<Goal>> getGoals();

}
