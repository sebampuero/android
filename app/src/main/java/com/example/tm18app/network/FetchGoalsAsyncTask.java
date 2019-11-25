package com.example.tm18app.network;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.tm18app.R;
import com.example.tm18app.adapters.MultiGoalSelectAdapter;
import com.example.tm18app.pojos.Goal;
import com.example.tm18app.pojos.GoalItemSelection;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

public class FetchGoalsAsyncTask extends AsyncTask<String, String, ArrayList<Goal>> {

    private MultiGoalSelectAdapter adapter ;
    private GoalsRestInterface restClient;
    private String[] selectedGoals;
    private WeakReference<Context> appContext;

    public FetchGoalsAsyncTask(MultiGoalSelectAdapter adapter, Context appContext) {
        this.adapter = adapter;
        this.appContext = new WeakReference<>(appContext);
        RetrofitNetworkConnectionSingleton retrofitNetworkConnectionSingleton = RetrofitNetworkConnectionSingleton.getInstance();
        restClient = retrofitNetworkConnectionSingleton.retrofitInstance().create(GoalsRestInterface.class);
    }

    public FetchGoalsAsyncTask(MultiGoalSelectAdapter adapter, String[] selectedGoals, Context appContext) {
        this.adapter = adapter;
        this.selectedGoals = selectedGoals;
        this.appContext = new WeakReference<>(appContext);
        RetrofitNetworkConnectionSingleton retrofitNetworkConnectionSingleton = RetrofitNetworkConnectionSingleton.getInstance();
        restClient = retrofitNetworkConnectionSingleton.retrofitInstance().create(GoalsRestInterface.class);
    }

    @Override
    protected ArrayList<Goal> doInBackground(String... strings) {
        Call<ArrayList<Goal>> call = restClient.getGoals();
        publishProgress();
        try {
            Response<ArrayList<Goal>> response = call.execute();
            if(response.code() == 500){
                return null;
            }
            if(response.code() == 200){
                return response.body();
            }
            else
                return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    protected void onPostExecute(ArrayList<Goal> goals) {
        if(goals == null){
            //TODO: no internet connection message!!
            return;
        }
        ArrayList<GoalItemSelection> array = new ArrayList<>();
        GoalItemSelection item;
        for(int i = 0; i < goals.size(); i++){
            item = new GoalItemSelection();
            item.setId(goals.get(i).getId());
            item.setTag(goals.get(i).getTag());
            item.setChecked(false);
            //TODO: check items that are already selected here
            array.add(item);
        }
        this.adapter.setGoals(array);
    }
}