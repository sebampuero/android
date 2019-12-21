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

import android.util.Log;
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
import com.example.tm18app.viewModels.ChatMessagesViewModel;
import com.example.tm18app.viewModels.MyViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatMessagesFragment extends BaseFragment implements ChatSocket.SocketListener {

    public static final String ROOM_ID = "roomId";
    public static final String ROOM_NAME = "roomName";
    public static final String TO_ID = "receiverId";
    public static final String TO_NAME = "to_name";
    public static final String PROFILE_PIC = "profile_pic";

    private FragmentChatMessagesBinding mBinding;
    private ChatMessagesViewModel mModel;
    private ChatMessagesAdapter mAdapter;
    private ArrayList<ChatMessage> mChatMessagesList = new ArrayList<>();
    private ChatSocket socket;
    private TextView mLoadingMessagesTv;
    private RecyclerView mRv;
    private Toolbar mToolbar;
    private String mProfilePicUrl;
    private ImageView mProfileIW;

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
        mModel = ViewModelProviders.of(this).get(ChatMessagesViewModel.class);
        mModel.setPrefs(mPrefs);
        mModel.setRoomId(getArguments().getString(ROOM_ID, null));
        mModel.setRoomName(getArguments().getString(ROOM_NAME, null));
        mModel.setToId(getArguments().getString(TO_ID));
        mModel.setToName(getArguments().getString(TO_NAME));
    }

    @Override
    public void onPause() {
        super.onPause();
        socket.detachListener();
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

    private void setupSocketConnection() {
        socket = new ChatSocket(getActivity(), mModel);
        socket.setSocketListener(this);
        if(mModel.getRoomName() == null || mModel.getRoomId() == null){
            socket.attachRoomListener();
        }else{
            mModel.callRepository();
        }
        socket.establishChat(mModel.getRoomName(),
                mPrefs.getInt(Constant.USER_ID, 0),
                Integer.parseInt(mModel.getToId()),
                mPrefs.getString(Constant.PUSHY_TOKEN, ""));
        mModel.setSocket(socket);
        socket.attachMessageListener();
        if(mModel.getMessagesLiveData().getValue() != null){
            mModel.getMessagesLiveData().getValue().clear();
        }
        socket.attachStatusListener();
        socket.attachTypingListener();
        socket.attachErrorListener();
        final Debouncer debouncer = new Debouncer();
        mModel.inputMessage.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                debouncer.debounce(Void.class, new Runnable() {
                    @Override
                    public void run() {
                        socket.sendTypingStatus(mModel.getRoomName());
                    }
                }, 500, TimeUnit.MILLISECONDS);
            }
        });
    }

    @Override
    protected void setupViews() {
        int profilePicDimen = getResources().getInteger(R.integer.thumbnail_profile_pic);
        mToolbar = ((MainActivity)getActivity()).getToolbar();
        mToolbar.getMenu().clear();
        mToolbar.setTitle(mModel.getToName());
        mToolbar.inflateMenu(R.menu.chat_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.deleteChat){
                    deleteChatRoom();
                }
                return false;
            }
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

    private void deleteChatRoom() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(getContext().getString(R.string.delete_chat));
        alertBuilder.setMessage(getContext().getString(R.string.delete_chat_conf_message));
        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                ChatsRepository repository = new ChatsRepository();
                repository.deleteChatRoom(mModel.getRoomId(),
                        mPrefs.getString(Constant.PUSHY_TOKEN, ""));
                mMainModel.getNavController().navigateUp();
                Toast.makeText(getContext(), getResources().getString(R.string.chat_room_deleted),
                        Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    private void fetchData() {
        mModel.getMessagesLiveData().observe(this, new Observer<List<ChatMessage>>() {
            @Override
            public void onChanged(List<ChatMessage> chatMessages) {
                if(chatMessages != null){
                    if(chatMessages.size() > 0){
                        mChatMessagesList.clear();
                        mChatMessagesList.addAll(chatMessages);
                        Collections.sort(mChatMessagesList);
                        mAdapter.notifyDataSetChanged();
                        mRv.scrollToPosition(mAdapter.getItemCount() - 1);
                    }
                }
                mLoadingMessagesTv.setVisibility(View.GONE);
            }
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
            mToolbar.setSubtitle("");
    }

    @Override
    public void onOtherTyping() {
        mToolbar.setSubtitle(getResources().getString(R.string.is_typing_a_message));
    }

    @Override
    public void onError(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
    }

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