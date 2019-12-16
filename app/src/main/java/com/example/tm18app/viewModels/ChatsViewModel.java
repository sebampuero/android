package com.example.tm18app.viewModels;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.model.Chat;
import com.example.tm18app.repository.ChatsRepository;

import java.util.List;

public class ChatsViewModel extends ViewModel {

    private LiveData<List<Chat>> chatLiveData;
    private SharedPreferences prefs;

    public LiveData<List<Chat>> getChatLiveData() {
        return chatLiveData;
    }

    public void setPrefs(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public void callRepository() {
        ChatsRepository repository = new ChatsRepository();
        String userId = String.valueOf(prefs.getInt(Constant.USER_ID, 0));
        this.chatLiveData = repository.getChatRooms(userId);
    }

}
