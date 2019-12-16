package com.example.tm18app.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tm18app.R;
import com.example.tm18app.adapters.ChatMessagesAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.FragmentChatMessagesBinding;
import com.example.tm18app.model.ChatMessage;
import com.example.tm18app.viewModels.ChatMessagesViewModel;
import com.example.tm18app.viewModels.MyViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatMessagesFragment extends Fragment {

    public static final String ROOM_ID = "roomId";

    private FragmentChatMessagesBinding mBinding;
    private MyViewModel mMainModel;
    private ChatMessagesViewModel mModel;
    private ChatMessagesAdapter mAdapter;
    private List<ChatMessage> mChatMessagesList = new ArrayList<>();

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
        mModel.setRoomId(getArguments().getString(ROOM_ID));
        fetchData();
        setupRecyclerView();
        return mBinding.getRoot();
    }

    private void fetchData() {
        mModel.callRepository();
        mModel.getMessagesLiveData().observe(this, new Observer<List<ChatMessage>>() {
            @Override
            public void onChanged(List<ChatMessage> chatMessages) {
                if(chatMessages.size() > 0){
                    mChatMessagesList.clear();
                    mChatMessagesList.addAll(chatMessages);
                    Collections.sort(mChatMessagesList);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
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
