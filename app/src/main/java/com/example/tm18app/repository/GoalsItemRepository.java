package com.example.tm18app.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tm18app.network.GoalsRestInterface;
import com.example.tm18app.network.RetrofitNetworkConnectionSingleton;
import com.example.tm18app.pojos.Goal;
import com.example.tm18app.pojos.GoalItemSelection;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for Goals responsible for managing the connection to the remote data source where
 * the model data is persisted. See <a href="https://developer.android.com/jetpack/docs/guide#overview">Jetpack architecture overview</a>
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class GoalsItemRepository {

    private GoalsRestInterface goalsRestInterface;

    public GoalsItemRepository() {
        goalsRestInterface = RetrofitNetworkConnectionSingleton.
                getInstance().retrofitInstance().create(GoalsRestInterface.class);
    }

    /**
     * Fetches a {@link List} of {@link Goal} from the API
     * @return {@link LiveData} containing the {@link List} of {@link Goal} items
     */
    public LiveData<List<Goal>> getGoals() {
        final MutableLiveData<List<Goal>> data = new MutableLiveData<>();
        goalsRestInterface.getGoals().enqueue(new Callback<List<Goal>>() {
            @Override
            public void onResponse(Call<List<Goal>> call, Response<List<Goal>> response) {
                if(response.body() != null){
                    data.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Goal>> call, Throwable t) {

            }
        });
        return data;
    }
}
