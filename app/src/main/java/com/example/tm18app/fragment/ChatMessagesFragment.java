package com.example.tm18app.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;
import com.example.tm18app.adapters.ChatMessagesAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.FragmentChatMessagesBinding;
import com.example.tm18app.model.ChatMessage;
import com.example.tm18app.network.ChatSocket;
import com.example.tm18app.viewModels.ChatMessagesViewModel;
import com.example.tm18app.viewModels.MyViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatMessagesFragment extends Fragment implements ChatSocket.SocketListener {

    public static final String ROOM_ID = "roomId";
    public static final String ROOM_NAME = "roomName";
    public static final String TO = "receiverId";

    private FragmentChatMessagesBinding mBinding;
    private MyViewModel mMainModel;
    private ChatMessagesViewModel mModel;
    private ChatMessagesAdapter mAdapter;
    private ArrayList<ChatMessage> mChatMessagesList = new ArrayList<>();
    private TextView mLoadingMessagesTv;
    private ChatSocket socket;

    public ChatMessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        mModel = ViewModelProviders.of(getActivity()).get(ChatMessagesViewModel.class);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_messages, container, false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        mModel.setRoomId(getArguments().getString(ROOM_ID, null));
        mModel.setRoomName(getArguments().getString(ROOM_NAME, null));
        SharedPreferences preferences = getActivity()
                .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        mModel.setPrefs(preferences);
        socket = new ChatSocket(getActivity(), mModel);
        if(mModel.getRoomName() == null){
            socket.attachRoomListener();
        }else{
            mModel.callRepository();
        }
        socket.setSocketListener(this);
        socket.establishChat(mModel.getRoomName(),
                preferences.getInt(Constant.USER_ID, 0),
                Integer.parseInt(getArguments().getString(TO)));
        mModel.setSocket(socket);
        socket.attachMessageListener();
        fetchData();
        setupViews();
        setupRecyclerView();
        return mBinding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.detachListener();
    }

    private void setupViews() {
        Toolbar toolbar = ((MainActivity)getActivity()).getToolbar();
        toolbar.getMenu().clear();
        mLoadingMessagesTv = mBinding.loadingMessagesTv;
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
    }

    @Override
    public void onRoomReceived() {
        mModel.callRepository();
    }

    private void setupRecyclerView() {
        RecyclerView rv = mBinding.chatMessagesRv;
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        rv.setLayoutManager(manager);
        SharedPreferences prefs = getActivity().getSharedPreferences(Constant.USER_INFO,
                Context.MODE_PRIVATE);
        mAdapter = new ChatMessagesAdapter(mChatMessagesList, prefs);
        rv.setAdapter(mAdapter);
    }


}
