package com.example.tm18app.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tm18app.model.ChatRoom;
import com.example.tm18app.model.ChatMessage;
import com.example.tm18app.network.ChatsRestInterface;
import com.example.tm18app.network.RetrofitNetworkConnectionSingleton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for chats responsible for managing network requests to the API.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class ChatsRepository {

    private ChatsRestInterface chatsRestInterface;

    public ChatsRepository() {
        chatsRestInterface = RetrofitNetworkConnectionSingleton.getInstance()
                .retrofitInstance().create(ChatsRestInterface.class);
    }

    /**
     * Retrieves the {@link ChatRoom} list of the logged in user.
     * @param userId {@link String} id of the logged in user
     * @param pushyToken {@link String} unique token of the logged in user
     * @return {@link MutableLiveData} containing the chat rooms
     */
    public LiveData<List<ChatRoom>> getChatRooms(String userId, String pushyToken) {
        final MutableLiveData<List<ChatRoom>> data = new MutableLiveData<>();
        chatsRestInterface.getRoomsByUserId(userId, pushyToken).enqueue(new Callback<List<ChatRoom>>() {
            @Override
            public void onResponse(Call<List<ChatRoom>> call, Response<List<ChatRoom>> response) {
                if(response.body() != null)
                    data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<List<ChatRoom>> call, Throwable t) {

            }
        });
        return data;
    }

    /**
     * Retrieves all {@link ChatMessage} of a given {@link ChatRoom}
     * @param roomId {@link String} id of the chat room
     * @param pushyToken {@link String} unique token of the logged in user
     * @param page {@link String} number of the page to load data. Used for pagination
     * @return {@link MutableLiveData} containing the chat messages
     */
    public LiveData<List<ChatMessage>> getChatsForRoom(String roomId, String pushyToken, String page) {
        final MutableLiveData<List<ChatMessage>> data = new MutableLiveData<>();
        chatsRestInterface.getChatMessagesByRoomId(roomId, pushyToken, page).enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {

            }
        });
        return data;
    }

    /**
     * Retrieves the total pages for the pagination of chat messages
     * @param roomId {@link String}
     * @param pushyToken {@link String}
     * @return the total number of pages
     */
    public LiveData<Integer> getTotalPagesForRoom(String roomId, String pushyToken) {
        final MutableLiveData<Integer> data = new MutableLiveData<>();
        chatsRestInterface.getTotalPagesForRoom(roomId, pushyToken).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
            }
        });
        return data;
    }

    /**
     * Deletes a {@link ChatRoom}. Upon deletion all {@link ChatMessage} get also lost.
     * @param roomId {@link String} id of the chat room
     * @param pushyToken {@link String} unique token of the logged in user
     */
    public void deleteChatRoom(String roomId, String pushyToken) {
        chatsRestInterface.deleteChatRoom(roomId, pushyToken).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }
}
