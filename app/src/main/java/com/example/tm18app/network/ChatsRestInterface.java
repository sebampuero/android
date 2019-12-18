package com.example.tm18app.network;


import com.example.tm18app.model.ChatRoom;
import com.example.tm18app.model.ChatMessage;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * {@link retrofit2.Retrofit} Interface for chat endpoints
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public interface ChatsRestInterface {

    @GET("api/goals/rooms/{userId}")
    Call<List<ChatRoom>> getRoomsByUserId(@Path("userId") String userId);

    @GET("api/goals/chats/{roomId}")
    Call<List<ChatMessage>> getChatMessagesByRoomId(@Path("roomId") String roomId);

}
