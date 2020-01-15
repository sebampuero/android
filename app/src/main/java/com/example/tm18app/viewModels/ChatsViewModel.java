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

    private MutableLiveData<Boolean> mReloadTrigger = new MutableLiveData<>();
    private SharedPreferences mPrefs;

    /**
     * Upon change on the {@link MutableLiveData} mReloadTrigger, the chatsLiveData is created
     * or updated. The mReloadTrigger is actuated when the ChatsView is loaded and reloaded
     * by a swipe. (Can also be programatically called)
     */
    private LiveData<List<ChatRoom>> chatLiveData = Transformations.switchMap(mReloadTrigger,
            new Function<Boolean, LiveData<List<ChatRoom>>>() {
        @Override
        public LiveData<List<ChatRoom>> apply(Boolean input) {
            ChatsRepository repository = new ChatsRepository();
            String userId = String.valueOf(mPrefs.getInt(Constant.USER_ID, 0));
            return repository.getChatRooms(userId, mPrefs.getString(Constant.PUSHY_TOKEN, ""));
        }
    });

    public LiveData<List<ChatRoom>> getChatLiveData() {
        return chatLiveData;
    }

    public void setPrefs(SharedPreferences prefs) {
        this.mPrefs = prefs;
    }

    public void callRepository() {
        mReloadTrigger.setValue(true);
    }

}
