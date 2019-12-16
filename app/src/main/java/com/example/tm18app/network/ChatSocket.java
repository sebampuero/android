package com.example.tm18app.network;

import com.example.tm18app.adapters.ChatMessagesAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.model.ChatMessage;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class ChatSocket {

    private Socket socket;
    private ChatMessagesAdapter chatMessagesAdapter;

    public ChatSocket(ChatMessagesAdapter adapter) {
        try {
            socket = IO.socket(Constant.API_ENDPOINT);
            socket.connect();
            chatMessagesAdapter = adapter;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void establishChat(String room, int senderId, int receiverId){
        socket.emit("enterChat", room, senderId, receiverId);
    }

    public void sendMessage(int userId, int roomId, String room, String message){
        socket.emit("message", userId, roomId, room, message);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomId(roomId);
        chatMessage.setSenderId(userId);
        chatMessage.setText(message);
        chatMessagesAdapter.addChatMessage(chatMessage);
    }
    
    //TODO: Add emitter for socket

}
