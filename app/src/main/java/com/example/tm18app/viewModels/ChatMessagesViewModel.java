package com.example.tm18app.viewModels;

import android.content.SharedPreferences;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.model.ChatMessage;
import com.example.tm18app.network.ChatSocket;
import com.example.tm18app.repository.ChatsRepository;

import java.util.List;

public class ChatMessagesViewModel extends ViewModel {

    public MutableLiveData<String> inputMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> reloadTrigger = new MutableLiveData<>();

    private LiveData<List<ChatMessage>> messagesLiveData = Transformations.switchMap(reloadTrigger, new Function<Boolean, LiveData<List<ChatMessage>>>() {
        @Override
        public LiveData<List<ChatMessage>> apply(Boolean input) {
            ChatsRepository repository = new ChatsRepository();
            return repository.getChatsForRoom(roomId);
        }
    });
    private String roomId;
    private String roomName;
    private SharedPreferences prefs;
    private ChatSocket socket;

    public LiveData<List<ChatMessage>> getMessagesLiveData() {
        return messagesLiveData;
    }

    public void onSendMessage() {
        if(inputMessage.getValue() != null)
            if(!inputMessage.getValue().equals("")){
                socket.sendMessage(prefs.getInt(Constant.USER_ID, 0),
                        Integer.parseInt(roomId),
                        roomName,
                        inputMessage.getValue());
                inputMessage.setValue("");
            }
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setPrefs(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public void callRepository() {
        reloadTrigger.setValue(true);
    }

    public void setSocket(ChatSocket socket) {
        this.socket = socket;
    }

    public void setRoomName(String room) {
        this.roomName = room;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getRoomId() {
        return roomId;
    }
}
