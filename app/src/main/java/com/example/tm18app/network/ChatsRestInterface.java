package com.example.tm18app.network;


import com.example.tm18app.model.ChatRoom;
import com.example.tm18app.model.ChatMessage;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

/**
 * {@link retrofit2.Retrofit} Interface for chat endpoints
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public interface ChatsRestInterface {

    @GET("api/chats/rooms/{userId}")
    Call<List<ChatRoom>> getRoomsByUserId(@Path("userId") String userId,
                                          @Header("pushy") String pushyToken);

    @GET("api/chats/{roomId}/{page}")
    Call<List<ChatMessage>> getChatMessagesByRoomId(@Path("roomId") String roomId, @Path("page") String page,
                                                    @Header("pushy") String pushyToken);

    @DELETE("api/chats/{roomId}")
    Call<Void> deleteChatRoom(@Path("roomId") String roomId,
                              @Header("pushy") String pushyToken);

}
