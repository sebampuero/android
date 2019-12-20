package com.example.tm18app.network;

import android.app.Activity;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.model.ChatMessage;
import com.example.tm18app.viewModels.ChatMessagesViewModel;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class ChatSocket {

    public static final int ONLINE = 1;
    public static final int OFFLINE = 0;
    private static final boolean DEBUG = true;

    public interface SocketListener {
        void onNewMessage(ChatMessage chatMessage);
        void onRoomReceived();
        void onOtherOnlineStatus(int status);
        void onOtherTyping();
    }

    private Timer timer;
    private SocketListener socketListener;
    private Socket socket;
    private Activity activity;
    private ChatMessagesViewModel chatsModel;

    public ChatSocket(Activity activity, ChatMessagesViewModel model) {
        try {
            if(DEBUG)
                socket = IO.socket(Constant.API_ENDPOINT_LOCAL);
            else
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

    public void establishChat(String room, int senderId, int receiverId, String pushyToken){
        socket.emit("enterChat", room, senderId, receiverId, pushyToken);
        timer = new Timer();
        if(room != null){
            startOnlineStatusBroadcaster(room);
        }
    }

    private void startOnlineStatusBroadcaster(final String room) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                socket.emit("clientStatusOnline", room);
            }
        },0,1000);
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

    public void sendTypingStatus(String room) {
        socket.emit("typing", room);
    }

    public void attachMessageListener() {
        MessageListener messageListener = new MessageListener();
        socket.on("message", messageListener);
    }

    public void attachRoomListener() {
        RoomCreationListener roomCreationListener = new RoomCreationListener();
        socket.on("room", roomCreationListener);
    }

    public void attachStatusListener() {
        OtherOnlineStatusListener statusListener = new OtherOnlineStatusListener();
        socket.on("status", statusListener);
    }

    public void attachTypingListener() {
        TypingListener listener = new TypingListener();
        socket.on("isTyping", listener);
    }

    public void detachListener() {
        socket.disconnect();
        socket.off();
        timer.cancel();
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
                    startOnlineStatusBroadcaster(String.valueOf(args[0]));
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

    class OtherOnlineStatusListener implements Emitter.Listener {

        @Override
        public void call(final Object... args) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    socketListener.onOtherOnlineStatus((Integer) args[0]);
                }
            });
        }
    }

    class TypingListener implements Emitter.Listener {

        @Override
        public void call(Object... args) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    socketListener.onOtherTyping();
                }
            });
        }
    }

}
