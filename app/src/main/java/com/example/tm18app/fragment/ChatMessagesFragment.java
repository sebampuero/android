package com.example.tm18app.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.R;
import com.example.tm18app.adapters.ChatMessagesAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.FragmentChatMessagesBinding;
import com.example.tm18app.model.ChatMessage;
import com.example.tm18app.network.ChatSocket;
import com.example.tm18app.repository.ChatsRepository;
import com.example.tm18app.util.Debouncer;
import com.example.tm18app.util.DialogManager;
import com.example.tm18app.util.TimeUtils;
import com.example.tm18app.viewModels.ChatMessagesViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
    private ChatSocket mSocket;
    private String mProfilePicUrl;
    private ChatMessagesViewModel mModel;
    private ChatMessagesAdapter mAdapter;
    private ArrayList<ChatMessage> mChatMessagesList = new ArrayList<>();
    private Debouncer mDebouncer;

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
        mModel.setContext(getContext());
    }

    @Override
    public void onPause() {
        // it is important to disconnect the mSocket when the user leaves the fragment
        // protect battery and bandwidth
        super.onPause();
        mSocket.detachListener(mPrefs.getInt(Constant.USER_ID, 0),
                Integer.parseInt(mModel.getRoomId()));
        mProfileIW.setVisibility(View.GONE);
        mToolbar.setSubtitle("");
        mToolbar.setOnClickListener(null);
        mDebouncer.shutdown();
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
        mSocket = new ChatSocket(requireActivity());
        mSocket.setmSocketListener(this);
        if(mModel.getRoomName() == null || mModel.getRoomId() == null){
            // when the user initializes a chatroom for the first time there is no chat room available
            // init a listener for the server to send the room mName and therefore start chatting
            mSocket.attachRoomListener();
        }else{
            // if there is a chat room already, retrieve the chat messages from the server
            mModel.callRepository();
        }
        mSocket.establishChat(mModel.getRoomName(),
                mPrefs.getInt(Constant.USER_ID, 0),
                Integer.parseInt(mModel.getToId()),
                mPrefs.getString(Constant.PUSHY_TOKEN, ""));
        mModel.setSocket(mSocket);
        mSocket.attachLastOnlineListener();
        mSocket.attachMessageListener();
        mSocket.attachRoomDeletedListener();
        if(mModel.getMessagesLiveData().getValue() != null){
            mModel.getMessagesLiveData().getValue().clear();
        }
        mSocket.attachStatusListener();
        mSocket.attachTypingListener();
        mSocket.attachErrorListener();
        // the chat room is capable of transmitting typing status. But it wastes network if done
        // for every key stroke, therefore use a mDebouncer to send keystrokes every half second
        mDebouncer = new Debouncer();
        mModel.mInputMessage.observe(this, keyInput -> mDebouncer.debounce(Void.class,
                () -> mSocket.sendTypingStatus(mModel.getRoomName()), 500, TimeUnit.MILLISECONDS));
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
        mProfileIW = requireActivity().findViewById(R.id.toolbarLogo);
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
        DialogManager
                .getInstance()
                .showAlertDialogSingleButton(
                        requireContext(),
                        requireContext().getString(R.string.delete_chat),
                        requireContext().getString(R.string.delete_chat_conf_message),
                        android.R.string.yes,
                        (dialogInterface, i) -> {
                            mSocket.transmitRoomDeleted(mModel.getRoomName());
                            ChatsRepository repository = new ChatsRepository();
                            repository.deleteChatRoom(mModel.getRoomId(),
                                    mPrefs.getString(Constant.PUSHY_TOKEN, ""));
                            mMainModel.getNavController().navigateUp();
                            Toast.makeText(requireContext(),
                                    getResources().getString(R.string.chat_room_deleted),
                                    Toast.LENGTH_SHORT).show();
                        }
                );
    }

    /**
     * Initializes an {@link Observer} for chat messages.
     */
    private void fetchData() {
        mModel.getMessagesLiveData().observe(this, chatMessages -> {
            if(chatMessages != null){
                if(chatMessages.size() > 0){
                    HashSet<ChatMessage> set = new HashSet<>(chatMessages);
                    set.addAll(mChatMessagesList);
                    mChatMessagesList.clear();
                    if(!mModel.isLoadingMoreItems()){
                        mChatMessagesList.addAll(set);
                        Collections.sort(mChatMessagesList);
                        mAdapter.notifyDataSetChanged();
                        mRv.scrollToPosition(mAdapter.getItemCount() - 1);
                    }else{ // append new messages to the beginning of list when user is scrolling UP
                        mChatMessagesList.addAll(0, set);
                        Collections.sort(mChatMessagesList);
                        mAdapter.notifyItemRangeInserted(0, chatMessages.size());
                        mLoadingMessagesTv.setVisibility(View.GONE);
                    }
                }
                mModel.setLoadingMoreItems(false);
            }else
                Toast.makeText(getContext(),
                        getString(R.string.cannot_load_messages), Toast.LENGTH_SHORT).show();
            mLoadingMessagesTv.setVisibility(View.GONE);
        });
    }

    @Override
    public void onNewMessage(ChatMessage chatMessage) {
        mChatMessagesList.add(chatMessage);
        mAdapter.notifyDataSetChanged();
        mRv.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    @Override
    public void onRoomReceived(String roomName, String roomId) {
        mModel.setRoomName(roomName);
        mModel.setRoomId(roomId);
        mModel.callRepository();
    }

    @Override
    public void onOtherOnlineStatus(int status) {
        if(status == ChatSocket.ONLINE)
            mToolbar.setSubtitle(getResources().getString(R.string.online));
        else if(status == ChatSocket.OFFLINE)
            mToolbar.setSubtitle(TimeUtils
                    .parseTimestampToLocaleDatetime(System.currentTimeMillis() / 1000L,
                            getContext()));
    }

    @Override
    public void onOtherLastOnline(int lastOnline) {
        if(lastOnline != 0){
            mToolbar.setSubtitle(TimeUtils.parseTimestampToLocaleDatetime(lastOnline, getContext()));
        }
    }

    @Override
    public void onOtherTyping() {
        mToolbar.setSubtitle(getResources().getString(R.string.is_typing_a_message));
    }

    @Override
    public void onError() {
        Toast.makeText(getContext(), getResources().getString(R.string.server_error),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoomDeleted() {
        Toast.makeText(getContext(),
                mModel.getToName() + " " + getString(R.string.chat_room_deleted_by_other),
                Toast.LENGTH_SHORT).show();
        mMainModel.getNavController().navigateUp();
    }

    /**
     * Sets up the {@link RecyclerView} for the chat messages.
     */
    private void setupRecyclerView() {
        mRv = mBinding.chatMessagesRv;
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        //manager.setStackFromEnd(true);
        mRv.setLayoutManager(manager);
        SharedPreferences prefs = requireActivity().getSharedPreferences(Constant.USER_INFO,
                Context.MODE_PRIVATE);
        mAdapter = new ChatMessagesAdapter(mChatMessagesList, prefs, getContext());
        mRv.setAdapter(mAdapter);
        mRv.addOnScrollListener(new CustomScrollListener((LinearLayoutManager)mRv.getLayoutManager()){
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if(!isLoading() && !lastPageReached()){
                    if(newState == RecyclerView.SCROLL_STATE_IDLE
                            && !recyclerView.canScrollVertically(-1)){
                        loadMoreItems();
                    }
                }
            }

            @Override
            boolean lastPageReached() {
                if(mModel.getTotalPagesLiveData().getValue() != null)
                    return mModel.getNumberPage() + 1 == mModel.getTotalPagesLiveData().getValue();
                return true;
            }

            @Override
            void loadMoreItems() {
                mModel.setLoadingMoreItems(true);
                mModel.setNumberPage(mModel.getNumberPage()+1);
                mModel.callRepository();
                mLoadingMessagesTv.setVisibility(View.VISIBLE);
            }

            @Override
            boolean isLoading() {
                return mModel.isLoadingMoreItems();
            }
        });
    }


}
