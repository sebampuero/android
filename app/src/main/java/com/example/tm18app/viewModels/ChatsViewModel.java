package com.example.tm18app.viewModels;

import android.content.SharedPreferences;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.model.ChatRoom;
import com.example.tm18app.repository.ChatsRepository;

import java.util.List;

/**
 * {@link ViewModel} class for the chat rooms UI
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class ChatsViewModel extends ViewModel {

    private MutableLiveData<Boolean> reloadTrigger = new MutableLiveData<>();
    private SharedPreferences prefs;

    /**
     * Upon change on the {@link MutableLiveData} reloadTrigger, the chatsLiveData is created
     * or updated. The reloadTrigger is actuated when the ChatsView is loaded and reloaded
     * by a swipe. (Can also be programatically called)
     */
    private LiveData<List<ChatRoom>> chatLiveData = Transformations.switchMap(reloadTrigger,
            new Function<Boolean, LiveData<List<ChatRoom>>>() {
        @Override
        public LiveData<List<ChatRoom>> apply(Boolean input) {
            ChatsRepository repository = new ChatsRepository();
            String userId = String.valueOf(prefs.getInt(Constant.USER_ID, 0));
            return repository.getChatRooms(userId, prefs.getString(Constant.PUSHY_TOKEN, ""));
        }
    });

    public LiveData<List<ChatRoom>> getChatLiveData() {
        return chatLiveData;
    }

    public void setPrefs(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public void callRepository() {
        reloadTrigger.setValue(true);
    }

}
