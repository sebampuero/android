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

/**
 * Class responsible for the communication via sockets with the server. Used for real time communication
 * with other users.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class ChatSocket {

    public static final int ONLINE = 1;
    public static final int OFFLINE = 0;
    private static final boolean DEBUG = false;

    /**
     * SocketListener for socket events
     */
    public interface SocketListener {

        /**
         * Called when a new message from the other user
         * @param chatMessage {@link ChatMessage}
         */
        void onNewMessage(ChatMessage chatMessage);

        /**
         * Called when the server sends back a created chat room. When a user initializes a chat
         * with another one, there is no room yet. The server creates a room and sends the response
         * as soon as the room is created.
         */
        void onRoomReceived();

        /**
         * Called when the other user's online status changes
         * @param status {@link Integer} online or offline
         */
        void onOtherOnlineStatus(int status);

        /**
         * Called when the other user is typing a message. Displays it on the {@link android.widget.Toolbar}
         */
        void onOtherTyping();

        /**
         * Called when there was an error.
         * @param error {@link String}
         */
        void onError(String error);
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

    /**
     * Sets the {@link SocketListener} for this ChatSocket
     * @param listener {@link SocketListener}
     */
    public void setSocketListener(SocketListener listener){
        this.socketListener = listener;
    }

    /**
     * Establishes a chat connection.
     * @param room {@link String} the chat room for this conversation. Null if no conversation existed
     *                           before
     * @param senderId {@link Integer}
     * @param receiverId {@link Integer}
     * @param pushyToken {@link String} auth token
     */
    public void establishChat(String room, int senderId, int receiverId, String pushyToken){
        socket.emit("enterChat", room, senderId, receiverId, pushyToken);
        timer = new Timer();
        if(room != null){ // init a timer to transmit online status to the other user
            startOnlineStatusBroadcaster(room);
        }
    }

    /**
     * Starts an online status broadcaster. When the user has the chat room opened, this broadcaster
     * transmits the online status event to the server
     * @param room {@link String}
     */
    private void startOnlineStatusBroadcaster(final String room) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                socket.emit("clientStatusOnline", room);
            }
        },0,5000); // every 5 seconds
    }

    /**
     * Transmits a message
     * @param userId {@link Integer}
     * @param roomId {@link Integer}
     * @param room {@link String}
     * @param message {@link String}
     */
    public void sendMessage(int userId, int roomId, String room, String message){
        socket.emit("message", userId, roomId, room, message);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomId(roomId);
        chatMessage.setSenderId(userId);
        chatMessage.setText(message);
        chatMessage.setTimestamp( System.currentTimeMillis() / 1000L);
        socketListener.onNewMessage(chatMessage);
    }

    /**
     * Transmits a typing status.
     * @param room {@link String}
     */
    public void sendTypingStatus(String room) {
        socket.emit("typing", room);
    }

    /**
     * Attaches the listener for incoming messages
     */
    public void attachMessageListener() {
        socket.on("message", new MessageListener());
    }

    /**
     * Attaches the listener for incoming room creations.
     */
    public void attachRoomListener() {
        socket.on("room",  new RoomCreationListener());
    }

    /**
     * Attaches an online status listener.
     */
    public void attachStatusListener() {
        socket.on("status", new OtherOnlineStatusListener());
    }

    /**
     * Attaches a typing status listener.
     */
    public void attachTypingListener() {
        socket.on("isTyping", new TypingListener());
    }

    /**
     * Attaches an error listener.
     */
    public void attachErrorListener() {
        socket.on("onError", new ErrorListener());
    }

    /**
     * Detaches all listeners for this {@link ChatSocket} and disconnects.
     */
    public void detachListener() {
        socket.disconnect();
        socket.off();
        timer.cancel();
    }

    /**
     * Listener for rooms.
     */
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

    /**
     * Listener for messages.
     */
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

    /**
     * Listener for online status.
     */
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

    /**
     * Listener for typing status.
     */
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

    /**
     * Listener for errors.
     */
    class ErrorListener implements Emitter.Listener {

        @Override
        public void call(final Object... args) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    socketListener.onError((String) args[0]);
                }
            });
        }
    }

}
