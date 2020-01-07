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

    public MutableLiveData<String> inputMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> reloadTrigger = new MutableLiveData<>();
    private String roomId;
    private String roomName;
    private String toId;
    private String toName;
    private int numberPage;
    private boolean isLoadingMoreItems;
    private LiveData<Integer> totalPagesLiveData = new MutableLiveData<>();

    private SharedPreferences prefs;
    private ChatSocket socket;
    private LiveData<List<ChatMessage>> messagesLiveData = Transformations.switchMap(reloadTrigger,
            new Function<Boolean, LiveData<List<ChatMessage>>>() {
        @Override
        public LiveData<List<ChatMessage>> apply(Boolean input) {
            ChatsRepository repository = new ChatsRepository();
            ChatMessagesViewModel.this.totalPagesLiveData =
                    repository.getTotalPagesForRoom(roomId, prefs.getString(Constant.PUSHY_TOKEN, ""));
            return repository.getChatsForRoom(roomId,
                    String.valueOf(numberPage), prefs.getString(Constant.PUSHY_TOKEN, ""));
        }
    });
    private Context context;

    public LiveData<List<ChatMessage>> getMessagesLiveData() {
        return messagesLiveData;
    }

    public LiveData<Integer> getTotalPagesLiveData() {
        return totalPagesLiveData;
    }

    public void onSendMessage() {
        if(inputMessage.getValue() != null)
            if(!inputMessage.getValue().equals("")){
                if(!NetworkConnectivity.isOnline(context)){
                    Toast.makeText(context,
                            context.getString(R.string.no_int_connection), Toast.LENGTH_SHORT).show();
                    inputMessage.setValue("");
                    return;
                }
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

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public int getNumberPage() {
        return numberPage;
    }

    public void setNumberPage(int numberPage) {
        this.numberPage = numberPage;
    }

    public boolean isLoadingMoreItems() {
        return isLoadingMoreItems;
    }

    public void setLoadingMoreItems(boolean loadingMoreItems) {
        isLoadingMoreItems = loadingMoreItems;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
