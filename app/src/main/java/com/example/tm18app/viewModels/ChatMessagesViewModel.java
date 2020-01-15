package com.example.tm18app.viewModels;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.model.ChatMessage;
import com.example.tm18app.network.ChatSocket;
import com.example.tm18app.network.NetworkConnectivity;
import com.example.tm18app.repository.ChatsRepository;

import java.util.List;

/**
 * {@link ViewModel} class for the chat messages UI
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class ChatMessagesViewModel extends ViewModel {

    public MutableLiveData<String> mInputMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> mReloadTrigger = new MutableLiveData<>();
    private String mRoomId;
    private String mRoomName;
    private String mToId;
    private String mToName;
    private int mNumberPage;
    private boolean mIsLoadingMoreItems;
    private LiveData<Integer> mTotalPagesLiveData = new MutableLiveData<>();

    private SharedPreferences mPrefs;
    private ChatSocket mSocket;
    private LiveData<List<ChatMessage>> mMessagesLiveData = Transformations.switchMap(mReloadTrigger,
            new Function<Boolean, LiveData<List<ChatMessage>>>() {
        @Override
        public LiveData<List<ChatMessage>> apply(Boolean input) {
            ChatsRepository repository = new ChatsRepository();
            ChatMessagesViewModel.this.mTotalPagesLiveData =
                    repository.getTotalPagesForRoom(mRoomId, mPrefs.getString(Constant.PUSHY_TOKEN, ""));
            return repository.getChatsForRoom(mRoomId,
                    String.valueOf(mNumberPage), mPrefs.getString(Constant.PUSHY_TOKEN, ""));
        }
    });
    private Context mContext;

    public LiveData<List<ChatMessage>> getMessagesLiveData() {
        return mMessagesLiveData;
    }

    public LiveData<Integer> getTotalPagesLiveData() {
        return mTotalPagesLiveData;
    }

    public void onSendMessage() {
        if(mInputMessage.getValue() != null)
            if(!mInputMessage.getValue().equals("")){
                if(!NetworkConnectivity.isOnline(mContext)){
                    Toast.makeText(mContext,
                            mContext.getString(R.string.no_int_connection), Toast.LENGTH_SHORT).show();
                    mInputMessage.setValue("");
                    return;
                }
                mSocket.sendMessage(mPrefs.getInt(Constant.USER_ID, 0),
                        Integer.parseInt(mRoomId),
                        mRoomName,
                        mInputMessage.getValue());
                mInputMessage.setValue("");
            }
    }

    public void setRoomId(String roomId) {
        this.mRoomId = roomId;
    }

    public void setPrefs(SharedPreferences prefs) {
        this.mPrefs = prefs;
    }

    public void callRepository() {
        mReloadTrigger.setValue(true);
    }

    public void setSocket(ChatSocket socket) {
        this.mSocket = socket;
    }

    public void setRoomName(String room) {
        this.mRoomName = room;
    }

    public String getRoomName() {
        return mRoomName;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public String getToId() {
        return mToId;
    }

    public void setToId(String toId) {
        this.mToId = toId;
    }

    public String getToName() {
        return mToName;
    }

    public void setToName(String toName) {
        this.mToName = toName;
    }

    public int getNumberPage() {
        return mNumberPage;
    }

    public void setNumberPage(int numberPage) {
        this.mNumberPage = numberPage;
    }

    public boolean isLoadingMoreItems() {
        return mIsLoadingMoreItems;
    }

    public void setLoadingMoreItems(boolean loadingMoreItems) {
        mIsLoadingMoreItems = loadingMoreItems;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }
}
