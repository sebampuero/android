package com.example.tm18app.network;

import android.app.Activity;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.devConfig.Config;
import com.example.tm18app.model.ChatMessage;
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

    /**
     * Status for when the user transmits on line
     */
    public static final int ONLINE = 1;
    /**
     * Status for when the user transmits offline
     */
    public static final int OFFLINE = 0;

    /**
     * SocketListener for mSocket events
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
         * @param roomName {@link String} the received room name
         * @param roomId {@link String} the received room id
         */
        void onRoomReceived(String roomName, String roomId);

        /**
         * Called when the other user's online status changes
         * @param status {@link Integer} online or offline
         */
        void onOtherOnlineStatus(int status);

        /**
         * Called when the receiver sends his/her online status
         * @param lastOnline
         */
        void onOtherLastOnline(int lastOnline);

        /**
         * Called when the other user is typing a message. Displays it on the {@link android.widget.Toolbar}
         */
        void onOtherTyping();

        /**
         * Called when there was an error.
         */
        void onError();

        /**
         * Called when the other user deletes the chat room
         */
        void onRoomDeleted();
    }

    private Timer mTimer;
    private SocketListener mSocketListener;
    private Socket mSocket;
    private Activity mActivity;

    public ChatSocket(Activity activity) {
        try {
            if(Config.DEBUG)
                mSocket = IO.socket(Constant.API_ENDPOINT_LOCAL);
            else
                mSocket = IO.socket(Constant.API_ENDPOINT);
            mSocket.connect();
            this.mActivity = activity;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the {@link SocketListener} for this ChatSocket
     * @param listener {@link SocketListener}
     */
    public void setmSocketListener(SocketListener listener){
        this.mSocketListener = listener;
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
        mSocket.emit("enterChat", room, senderId, receiverId, pushyToken);
        mTimer = new Timer();
        if(room != null){ // init a mTimer to transmit online status to the other user
            startOnlineStatusBroadcaster(room);
        }
    }

    /**
     * Starts an online status broadcaster. When the user has the chat room opened, this broadcaster
     * transmits the online status event to the server
     * @param room {@link String}
     */
    private void startOnlineStatusBroadcaster(final String room) {
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mSocket.emit("clientStatusOnline", room);
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
        mSocket.emit("message", userId, roomId, room, message);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomId(roomId);
        chatMessage.setSenderId(userId);
        chatMessage.setText(message);
        chatMessage.setTimestamp( System.currentTimeMillis() / 1000L);
        mSocketListener.onNewMessage(chatMessage);
    }

    /**
     * Transmits a typing status.
     * @param room {@link String}
     */
    public void sendTypingStatus(String room) {
        mSocket.emit("typing", room);
    }

    /**
     * Attaches the listener for incoming messages
     */
    public void attachMessageListener() {
        mSocket.on("message", args -> mActivity.runOnUiThread(() -> {
            ChatMessage message = new ChatMessage();
            message.setRoomId((Integer) args[0]);
            message.setSenderId((Integer) args[1]);
            message.setText((String) args[2]);
            message.setTimestamp((Integer) args[3]);
            mSocketListener.onNewMessage(message);
        }));
    }

    /**
     * Attaches the listener for incoming room creations.
     */
    public void attachRoomListener() {
        mSocket.on("room", args -> mActivity.runOnUiThread(() -> {
            mSocketListener.onRoomReceived(String.valueOf(args[0]), String.valueOf(args[1]));
            startOnlineStatusBroadcaster(String.valueOf(args[0]));
        }));
    }

    /**
     * Attaches an online status listener.
     */
    public void attachStatusListener() {
        mSocket.on("status", args -> mActivity.runOnUiThread(()
                -> mSocketListener.onOtherOnlineStatus((Integer) args[0])));
    }

    /**
     * Attaches a typing status listener.
     */
    public void attachTypingListener() {
        mSocket.on("isTyping", args -> mActivity.runOnUiThread(() -> mSocketListener.onOtherTyping()));
    }

    /**
     * Attaches an error listener.
     */
    public void attachErrorListener() {
        mSocket.on("errorEvent", args -> mActivity.runOnUiThread(() -> mSocketListener.onError()));
    }

    public void attachLastOnlineListener() {
        mSocket.on("lastOnline", args -> mActivity.runOnUiThread(()
                -> mSocketListener.onOtherLastOnline((int) args[0])));
    }

    public void attachRoomDeletedListener() {
        mSocket.on("roomDeleted", args ->
                mActivity.runOnUiThread(() -> mSocketListener.onRoomDeleted()));
    }

    public void transmitRoomDeleted(String room){
        mSocket.emit("roomDeleted", room);
    }

    /**
     * Detaches all listeners for this {@link ChatSocket} and disconnects.
     */
    public void detachListener(int userId, int roomid) {
        mSocket.emit("beforeDisconnect", userId, roomid);
        mSocket.disconnect();
        mSocket.off();
        mTimer.cancel();
    }
}
