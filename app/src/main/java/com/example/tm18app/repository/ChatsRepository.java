package com.example.tm18app.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tm18app.model.Chat;
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

    public LiveData<List<Chat>> getChatRooms(String userId) {
        final MutableLiveData<List<Chat>> data = new MutableLiveData<>();
        chatsRestInterface.getRoomsByUserId(userId).enqueue(new Callback<List<Chat>>() {
            @Override
            public void onResponse(Call<List<Chat>> call, Response<List<Chat>> response) {
                if(response.body() != null)
                    data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<List<Chat>> call, Throwable t) {

            }
        });
        return data;
    }

    public LiveData<List<ChatMessage>> getChatsForRoom(String roomId) {
        final MutableLiveData<List<ChatMessage>> data = new MutableLiveData<>();
        chatsRestInterface.getChatMessagesByRoomId(roomId).enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if(response.body() != null)
                    data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {

            }
        });
        return data;
    }
}
