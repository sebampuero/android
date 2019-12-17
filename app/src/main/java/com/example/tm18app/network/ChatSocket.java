package com.example.tm18app.network;

import com.example.tm18app.MainActivity;
import com.example.tm18app.adapters.ChatMessagesAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.model.ChatMessage;
import com.example.tm18app.viewModels.ChatMessagesViewModel;
import com.example.tm18app.viewModels.ChatsViewModel;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class ChatSocket {

    private Socket socket;
    private ChatMessagesAdapter chatMessagesAdapter;
    private MainActivity activity;
    private MessageListener messageListener;
    private RoomListener roomListener;
    private ChatMessagesViewModel chatsModel;

    public ChatSocket(ChatMessagesAdapter adapter, MainActivity activity, ChatMessagesViewModel model) {
        try {
            socket = IO.socket(Constant.API_ENDPOINT);
            socket.connect();
            chatMessagesAdapter = adapter;
            this.activity = activity;
            this.chatsModel = model;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void establishChat(String room, int senderId, int receiverId){
        if(room == null)
            socket.emit("enterChat", senderId, receiverId);
        else
            socket.emit("enterChat", room, senderId, receiverId);
    }

    public void sendMessage(int userId, int roomId, String room, String message){
        socket.emit("message", userId, roomId, room, message);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomId(roomId);
        chatMessage.setSenderId(userId);
        chatMessage.setText(message);
        chatMessage.setTimestamp( System.currentTimeMillis() / 1000L);
        chatMessagesAdapter.addChatMessage(chatMessage);
    }

    public void attachMessageListener() {
        messageListener = new MessageListener();
        socket.on("message", messageListener);
    }

    public void attachRoomListener() {
        roomListener = new RoomListener();
        socket.on("room", roomListener);
    }

    public void detachListener() {
        socket.disconnect();
        socket.off();
    }

    class RoomListener implements Emitter.Listener {

        @Override
        public void call(final Object... args) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatsModel.setRoomName(String.valueOf(args[0]));
                }
            });
        }
    }

    class MessageListener implements Emitter.Listener {

        @Override
        public void call(final Object... args) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ChatMessage message = new ChatMessage();
                    message.setRoomId((Integer) args[0]);
                    message.setSenderId((Integer) args[1]);
                    message.setText((String) args[2]);
                    message.setTimestamp((Long) args[3]);
                    chatMessagesAdapter.addChatMessage(message);
                }
            });
        }
    }

}
