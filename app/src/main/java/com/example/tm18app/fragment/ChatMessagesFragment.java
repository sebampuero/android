package com.example.tm18app.fragment;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

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
import com.example.tm18app.repository.PostItemRepository;
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
    public static final String TO_ID = "receiverId";
    public static final String TO_NAME = "to_name";

    private FragmentChatMessagesBinding mBinding;
    private ChatMessagesViewModel mModel;
    private MyViewModel mMainModel;
    private ChatMessagesAdapter mAdapter;
    private ArrayList<ChatMessage> mChatMessagesList = new ArrayList<>();
    private ChatSocket socket;
    private SharedPreferences mPreferences;
    private TextView mLoadingMessagesTv;
    private RecyclerView mRv;

    public ChatMessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        mModel = ViewModelProviders.of(this).get(ChatMessagesViewModel.class);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_messages, container, false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        mPreferences = getActivity()
                .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        mModel.setPrefs(mPreferences);
        setupSocketConnection();
        setupViews();
        setupRecyclerView();
        fetchData();
        return mBinding.getRoot();
    }

    private void setupSocketConnection() {
        mModel.setRoomId(getArguments().getString(ROOM_ID, null));
        mModel.setRoomName(getArguments().getString(ROOM_NAME, null));
        socket = new ChatSocket(getActivity(), mModel);
        if(mModel.getRoomName() == null || mModel.getRoomId() == null){
            socket.attachRoomListener();
        }else{
            mModel.callRepository();
        }
        socket.setSocketListener(this);
        socket.establishChat(mModel.getRoomName(),
                mPreferences.getInt(Constant.USER_ID, 0),
                Integer.parseInt(getArguments().getString(TO_ID)));
        mModel.setSocket(socket);
        socket.attachMessageListener();
        if(mModel.getMessagesLiveData().getValue() != null){
            mModel.getMessagesLiveData().getValue().clear();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.detachListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        mModel.getMessagesLiveData().removeObservers(this);
    }

    private void setupViews() {
        Toolbar toolbar = ((MainActivity)getActivity()).getToolbar();
        toolbar.getMenu().clear();
        toolbar.setTitle(getArguments().getString(TO_NAME));
        toolbar.inflateMenu(R.menu.chat_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.deleteChat){
                    deleteChatRoom();
                }
                return false;
            }
        });
        mLoadingMessagesTv = mBinding.loadingMessagesTv;
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
                repository.deleteChatRoom(mModel.getRoomId());
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
