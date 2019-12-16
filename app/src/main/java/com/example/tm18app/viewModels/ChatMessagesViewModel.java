package com.example.tm18app.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tm18app.model.ChatMessage;
import com.example.tm18app.repository.ChatsRepository;

import java.util.List;

public class ChatMessagesViewModel extends ViewModel {

    public MutableLiveData<String> inputMessage;
    private LiveData<List<ChatMessage>> messagesLiveData;
    private String roomId;

    public LiveData<List<ChatMessage>> getMessagesLiveData() {
        return messagesLiveData;
    }

    public void onSendMessage() {

    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void callRepository() {
        ChatsRepository repository = new ChatsRepository();
        this.messagesLiveData = repository.getChatsForRoom(this.roomId);
    }

}
