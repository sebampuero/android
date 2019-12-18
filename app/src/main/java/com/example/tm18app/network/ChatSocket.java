package com.example.tm18app.network;

import android.app.Activity;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.model.ChatMessage;
import com.example.tm18app.viewModels.ChatMessagesViewModel;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class ChatSocket {

    public interface SocketListener {
        void onNewMessage(ChatMessage chatMessage);
        void onRoomReceived();
    }

    private SocketListener socketListener;
    private Socket socket;
    private Activity activity;
    private MessageListener messageListener;
    private RoomCreationListener roomCreationListener;
    private ChatMessagesViewModel chatsModel;

    public ChatSocket(Activity activity, ChatMessagesViewModel model) {
        try {
            socket = IO.socket(Constant.API_ENDPOINT);
            socket.connect();
            this.activity = activity;
            this.chatsModel = model;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void setSocketListener(SocketListener listener){
        this.socketListener = listener;
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
        chatMessage.setTimestamp( System.currentTimeMillis() / 1000L);
        socketListener.onNewMessage(chatMessage);
    }

    public void attachMessageListener() {
        messageListener = new MessageListener();
        socket.on("message", messageListener);
    }

    public void attachRoomListener() {
        roomCreationListener = new RoomCreationListener();
        socket.on("room", roomCreationListener);
    }

    public void detachListener() {
        socket.disconnect();
        socket.off();
    }

    class RoomCreationListener implements Emitter.Listener {
        @Override
        public void call(final Object... args) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatsModel.setRoomName(String.valueOf(args[0]));
                    chatsModel.setRoomId(String.valueOf(args[1]));
                    socketListener.onRoomReceived();
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
                    message.setTimestamp((Integer) args[3]);
                    socketListener.onNewMessage(message);
                }
            });
        }
    }

}
