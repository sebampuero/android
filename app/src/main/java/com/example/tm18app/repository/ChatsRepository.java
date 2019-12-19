package com.example.tm18app.repository;

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

public class ChatsRepository {

    private ChatsRestInterface chatsRestInterface;

    public ChatsRepository() {
        chatsRestInterface = RetrofitNetworkConnectionSingleton.getInstance()
                .retrofitInstance().create(ChatsRestInterface.class);
    }

    public LiveData<List<ChatRoom>> getChatRooms(String userId) {
        final MutableLiveData<List<ChatRoom>> data = new MutableLiveData<>();
        chatsRestInterface.getRoomsByUserId(userId).enqueue(new Callback<List<ChatRoom>>() {
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

    public LiveData<List<ChatMessage>> getChatsForRoom(String roomId) {
        final MutableLiveData<List<ChatMessage>> data = new MutableLiveData<>();
        chatsRestInterface.getChatMessagesByRoomId(roomId).enqueue(new Callback<List<ChatMessage>>() {
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

    public void deleteChatRoom(String roomId) {
        chatsRestInterface.deleteChatRoom(roomId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }
}
