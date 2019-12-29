package com.example.tm18app.network;

import com.example.tm18app.constants.Constant;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton for a {@link Retrofit} instance
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class RetrofitNetworkConnectionSingleton {

    private static RetrofitNetworkConnectionSingleton singleInstance = null;
    private static Retrofit retrofit;
    private Gson gson;
    private static final boolean DEBUG = false;

    private RetrofitNetworkConnectionSingleton(){
        // network initialization
        gson = new GsonBuilder()
                .setLenient()
                .create();
        if(DEBUG){
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constant.API_ENDPOINT_LOCAL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }else{
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constant.API_ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
    }

    /**
     * Returns the {@link Retrofit} instance
     * @return {@link Retrofit}
     */
    public static RetrofitNetworkConnectionSingleton getInstance(){
        if(singleInstance == null)
            singleInstance = new RetrofitNetworkConnectionSingleton();
        return singleInstance;
    }

    public Retrofit retrofitInstance(){
        return retrofit;
    }

}
