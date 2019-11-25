package com.example.tm18app.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitNetworkConnectionSingleton {

    private static RetrofitNetworkConnectionSingleton singleInstance = null;
    private static Retrofit retrofit;
    private Gson gson;

    private RetrofitNetworkConnectionSingleton(){
        // network initialization
        gson = new GsonBuilder()
                .setLenient()
                .create();
        retrofit = new Retrofit.Builder()
                .baseUrl("https://goalsapp-api.herokuapp.com")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static RetrofitNetworkConnectionSingleton getInstance(){
        if(singleInstance == null)
            singleInstance = new RetrofitNetworkConnectionSingleton();
        return singleInstance;
    }

    public Retrofit retrofitInstance(){
        return retrofit;
    }

}
