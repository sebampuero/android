package com.example.tm18app.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tm18app.network.RetrofitNetworkConnectionSingleton;
import com.example.tm18app.network.UserRestInterface;
import com.example.tm18app.pojos.User;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private UserRestInterface userRestInterface;

    public UserRepository() {
        userRestInterface = RetrofitNetworkConnectionSingleton
                .getInstance().retrofitInstance().create(UserRestInterface.class);
    }

    public void loginUser(User user, MutableLiveData<HashMap<Integer, User>> userLiveData){
        final MutableLiveData<HashMap<Integer, User>> responseCode = userLiveData;
        userRestInterface.loginUser(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                HashMap<Integer, User> hashMap = new HashMap<>();
                hashMap.put(response.code(), response.body());
                responseCode.setValue(hashMap);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    public void registerUser(User user, MutableLiveData<HashMap<Integer, User>> userLiveData){
        final MutableLiveData<HashMap<Integer, User>> responseCode = userLiveData;
        userRestInterface.registerUser(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                HashMap<Integer, User> hashMap = new HashMap<>();
                hashMap.put(response.code(), response.body());
                responseCode.setValue(hashMap);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }


    public void editUser(User user, MutableLiveData<HashMap<Integer, User>> userLiveData) {
        final MutableLiveData<HashMap<Integer, User>> responseCode = userLiveData;
        userRestInterface.updateUser(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                HashMap<Integer, User> hashMap = new HashMap<>();
                hashMap.put(response.code(), response.body());
                responseCode.setValue(hashMap);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }
}
