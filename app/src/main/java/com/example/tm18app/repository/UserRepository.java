package com.example.tm18app.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tm18app.network.RegisterUserAsyncTask;
import com.example.tm18app.network.RetrofitNetworkConnectionSingleton;
import com.example.tm18app.network.UserRestInterface;
import com.example.tm18app.model.PasswordReset;
import com.example.tm18app.model.User;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for {@link User} responsible for managing the connection to the remote data source where
 * the model data is persisted. See <a href="https://developer.android.com/jetpack/docs/guide#overview">Jetpack architecture overview</a>
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class UserRepository {

    private UserRestInterface mUserRestInterface;

    public UserRepository() {
        mUserRestInterface = RetrofitNetworkConnectionSingleton
                .getInstance().retrofitInstance().create(UserRestInterface.class);
    }

    /**
     * Logs in a {@link User} by sending a POST Request to the API
     * @param user {@link User} the user to be logged in
     * @param userLiveData {@link MutableLiveData} containing a {@link HashMap} for the HTTP Status code and response body that contains information about the logged in user
     */
    public void loginUser(User user, MutableLiveData<HashMap<Integer, User>> userLiveData){
        final MutableLiveData<HashMap<Integer, User>> responseCode = userLiveData;
        mUserRestInterface.loginUser(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                HashMap<Integer, User> hashMap = new HashMap<>();
                // response.body() is the serialized User Model containing the information sent from the API
                hashMap.put(response.code(), response.body());
                responseCode.setValue(hashMap);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    /**
     * Registers a {@link User} by sending a POST Request to the API
     * @param user {@link User} the user to be registered
     * @param userLiveData {@link MutableLiveData} containing a {@link HashMap} for the HTTP Status code and response body that contains information about the registered user
     */
    public void registerUser(User user, MutableLiveData<HashMap<Integer, User>> userLiveData, Context ctx){
        final MutableLiveData<HashMap<Integer, User>> responseMappingMutableLiveData = userLiveData;
        new RegisterUserAsyncTask(ctx, user, mUserRestInterface, responseMappingMutableLiveData).execute();
    }


    /**
     * Edits info of a {@link User} by sending a POST Request to the API
     * @param user {@link User} the user to be edited
     * @param userLiveData {@link MutableLiveData} containing a {@link HashMap} for the HTTP Status code and response body that contains information about the edited user
     * @param pushyToken {@link String} unique token of the logged in user
     */
    public void editUser(User user, MutableLiveData<HashMap<Integer,
                            User>> userLiveData, String pushyToken) {
        final MutableLiveData<HashMap<Integer, User>> responseCode = userLiveData;
        mUserRestInterface.updateUser(user, pushyToken).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                HashMap<Integer, User> hashMap = new HashMap<>();
                // response.body() is the serialized User Model containing the information sent from the API
                hashMap.put(response.code(), response.body());
                responseCode.setValue(hashMap);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    /**
     * Makes a request to the server to change the user's password
     * @param passwordReset {@link PasswordReset} containing old password, new password and user id
     * @param statusCodeResponseLiveData {@link MutableLiveData} to emit status response to the UI
     * @param pushyToken {@link String} unique token of the logged in user
     */
    public void changeUserPassword(PasswordReset passwordReset,
                                   MutableLiveData<Integer> statusCodeResponseLiveData,
                                   String pushyToken){
        final MutableLiveData<Integer> statusCodeRespIntegerMutableLiveData = statusCodeResponseLiveData;
        mUserRestInterface.updatePassword(passwordReset, pushyToken).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                statusCodeRespIntegerMutableLiveData.setValue(response.code());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                statusCodeRespIntegerMutableLiveData.setValue(0);
            }
        });
    }

    /**
     * Retrieves {@link User} details
     * @param id {@link String} id of the user to retrieve details from
     * @param pushyToken {@link String} unique token of the logged in user
     * @return {@link MutableLiveData} containing user's details
     */
    public LiveData<User> getUser(String id, String pushyToken) {
        final MutableLiveData<User> data = new MutableLiveData<>();
        mUserRestInterface.getUserById(id, pushyToken).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.body() != null){
                    data.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
        return data;
    }
}
