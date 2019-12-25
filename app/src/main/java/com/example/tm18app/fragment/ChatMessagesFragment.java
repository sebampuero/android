package com.example.tm18app.fragment;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;
import com.example.tm18app.adapters.ChatMessagesAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.FragmentChatMessagesBinding;
import com.example.tm18app.model.ChatMessage;
import com.example.tm18app.network.ChatSocket;
import com.example.tm18app.repository.ChatsRepository;
import com.example.tm18app.util.Debouncer;
import com.example.tm18app.util.TimeUtils;
import com.example.tm18app.viewModels.ChatMessagesViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass. This class represents the UI for  a given {@link com.example.tm18app.model.ChatRoom}
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class ChatMessagesFragment extends BaseFragment implements ChatSocket.SocketListener {

    // static vars
    public static final String ROOM_ID = "roomId";
    public static final String ROOM_NAME = "roomName";
    public static final String TO_ID = "receiverId";
    public static final String TO_NAME = "to_name";
    public static final String PROFILE_PIC = "profile_pic";

    // views
    private FragmentChatMessagesBinding mBinding;
    private TextView mLoadingMessagesTv;
    private RecyclerView mRv;
    private ImageView mProfileIW;

    // vars
    private ChatSocket socket;
    private String mProfilePicUrl;
    private ChatMessagesViewModel mModel;
    private ChatMessagesAdapter mAdapter;
    private ArrayList<ChatMessage> mChatMessagesList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(getArguments().getString(PROFILE_PIC) != null){
            mProfilePicUrl = getArguments().getString(PROFILE_PIC);
        }
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_messages, container, false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set relevant recipients info on the VM
        mModel = ViewModelProviders.of(this).get(ChatMessagesViewModel.class);
        mModel.setPrefs(mPrefs);
        mModel.setRoomId(getArguments().getString(ROOM_ID, null));
        mModel.setRoomName(getArguments().getString(ROOM_NAME, null));
        mModel.setToId(getArguments().getString(TO_ID));
        mModel.setToName(getArguments().getString(TO_NAME));
    }

    @Override
    public void onPause() {
        // it is important to disconnect the socket when the user leaves the fragment
        // protect battery and bandwidth
        super.onPause();
        socket.detachListener(mPrefs.getInt(Constant.USER_ID, 0),
                Integer.parseInt(mModel.getRoomId()));
        mToolbar.setSubtitle("");
        mProfileIW.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupSocketConnection();
        setupViews();
        setupRecyclerView();
        fetchData();
    }

    /**
     * Sets up the {@link ChatSocket} for the chat.
     */
    private void setupSocketConnection() {
        socket = new ChatSocket(getActivity(), mModel);
        socket.setSocketListener(this);
        if(mModel.getRoomName() == null || mModel.getRoomId() == null){
            // when the user initializes a chatroom for the first time there is no chat room available
            // init a listener for the server to send the room name and therefore start chatting
            socket.attachRoomListener();
        }else{
            // if there is a chat room already, retrieve the chat messages from the server
            mModel.callRepository();
        }
        socket.establishChat(mModel.getRoomName(),
                mPrefs.getInt(Constant.USER_ID, 0),
                Integer.parseInt(mModel.getToId()),
                mPrefs.getString(Constant.PUSHY_TOKEN, ""));
        mModel.setSocket(socket);
        socket.attachLastOnlineListener();
        socket.attachMessageListener();
        if(mModel.getMessagesLiveData().getValue() != null){
            mModel.getMessagesLiveData().getValue().clear();
        }
        socket.attachStatusListener();
        socket.attachTypingListener();
        socket.attachErrorListener();
        // the chat room is capable of transmitting typing status. But it wastes network if done
        // for every key stroke, therefore use a debouncer to send keystrokes every half second
        final Debouncer debouncer = new Debouncer();
        mModel.inputMessage.observe(this, keyInput -> debouncer.debounce(Void.class,
                () -> socket.sendTypingStatus(mModel.getRoomName()), 500, TimeUnit.MILLISECONDS));
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        int profilePicDimen = getResources().getInteger(R.integer.thumbnail_profile_pic);
        mToolbar.setTitle(mModel.getToName());
        mToolbar.inflateMenu(R.menu.chat_menu);
        mToolbar.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.deleteChat){
                deleteChatRoom();
            }
            return false;
        });
        mToolbar.setOnClickListener(view -> {
            Bundle b = new Bundle();
            b.putString(OtherProfileFragment.OTHER_USER_ID, mModel.getToId());
            mMainModel.getNavController().navigate(R.id.otherProfileFragment, b);
        });
        mLoadingMessagesTv = mBinding.loadingMessagesTv;
        mProfileIW = getActivity().findViewById(R.id.toolbarLogo);
        if(mProfilePicUrl != null){
            mProfileIW.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(mProfilePicUrl)
                    .resize(profilePicDimen, profilePicDimen)
                    .centerCrop()
                    .into(mProfileIW);
        }else
            mProfileIW.setVisibility(View.GONE);
    }

    /**
     * Deletes the chat room. <b>Warning: it deletes chats for both users.</b> Still a bad practice, would
     * be good to implement a different approach in the future.
     */
    private void deleteChatRoom() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(getContext().getString(R.string.delete_chat));
        alertBuilder.setMessage(getContext().getString(R.string.delete_chat_conf_message));
        alertBuilder.setPositiveButton(android.R.string.yes, (dialogInterface, which) -> {
            ChatsRepository repository = new ChatsRepository();
            repository.deleteChatRoom(mModel.getRoomId(),
                    mPrefs.getString(Constant.PUSHY_TOKEN, ""));
            mMainModel.getNavController().navigateUp();
            Toast.makeText(getContext(), getResources().getString(R.string.chat_room_deleted),
                    Toast.LENGTH_SHORT).show();
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    /**
     * Initializes an {@link Observer} for chat messages.
     */
    private void fetchData() {
        mModel.getMessagesLiveData().observe(this, chatMessages -> {
            if(chatMessages != null){
                if(chatMessages.size() > 0){
                    mChatMessagesList.clear();
                    mChatMessagesList.addAll(chatMessages);
                    Collections.sort(mChatMessagesList);
                    mAdapter.notifyDataSetChanged();
                    mRv.scrollToPosition(mAdapter.getItemCount() - 1); // scroll to bottom for
                    // better UX
                }
            }
            mLoadingMessagesTv.setVisibility(View.GONE);
        });
    }

    @Override
    public void onNewMessage(ChatMessage chatMessage) {
        ArrayList<ChatMessage> messages;
        messages = (ArrayList<ChatMessage>) mChatMessagesList.clone();
        messages.add(chatMessage);
        mChatMessagesList.clear();
        mChatMessagesList.addAll(messages);
        mAdapter.notifyDataSetChanged();
        mRv.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    @Override
    public void onRoomReceived() {
        mModel.callRepository();
    }

    @Override
    public void onOtherOnlineStatus(int status) {
        if(status == ChatSocket.ONLINE)
            mToolbar.setSubtitle(getResources().getString(R.string.online));
        else if(status == ChatSocket.OFFLINE)
            mToolbar.setSubtitle(TimeUtils.parseTimestampToLocaleDatetime(System.currentTimeMillis() / 1000L));
    }

    @Override
    public void onOtherLastOnline(int lastOnline) {
        if(lastOnline != 0){
            mToolbar.setSubtitle(TimeUtils.parseTimestampToLocaleDatetime(lastOnline));
        }
    }

    @Override
    public void onOtherTyping() {
        mToolbar.setSubtitle(getResources().getString(R.string.is_typing_a_message));
    }

    @Override
    public void onError(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
    }

    /**
     * Sets up the {@link RecyclerView} for the chat messages.
     */
    private void setupRecyclerView() {
        mRv = mBinding.chatMessagesRv;
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setStackFromEnd(true);
        mRv.setLayoutManager(manager);
        SharedPreferences prefs = getActivity().getSharedPreferences(Constant.USER_INFO,
                Context.MODE_PRIVATE);
        mAdapter = new ChatMessagesAdapter(mChatMessagesList, prefs);
        mRv.setAdapter(mAdapter);
    }


}
