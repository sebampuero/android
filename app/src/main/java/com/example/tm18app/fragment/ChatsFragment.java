package com.example.tm18app.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;
import com.example.tm18app.adapters.ChatsAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.FragmentChatsBinding;
import com.example.tm18app.model.ChatRoom;
import com.example.tm18app.viewModels.ChatsViewModel;
import com.example.tm18app.viewModels.MyViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends BaseFragment {

    private FragmentChatsBinding mBinding;
    private ChatsViewModel mModel;
    private ChatsAdapter mAdapter;
    private ProgressBar mChatsProgressView;
    private TextView mNoChatsTV;
    private List<ChatRoom> mChatsList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefresh;
    private RecyclerView mRv;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mModel = ViewModelProviders.of(getActivity()).get(ChatsViewModel.class);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_chats,container, false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        mModel.setPrefs(mPrefs);
        setupViews();
        setupRecyclerView();
        fetchData();
        return mBinding.getRoot();
    }

    @Override
    protected void setupViews() {
        Toolbar toolbar = ((MainActivity)getActivity()).getToolbar();
        toolbar.getMenu().clear();
        mChatsProgressView = mBinding.chatsProgressView;
        mNoChatsTV = mBinding.noChatsTv;
        mSwipeRefresh = mBinding.swipeRefreshLayoutChats;
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mModel.callRepository();
            }
        });
    }

    private void fetchData() {
        mModel.callRepository();
        mModel.getChatLiveData().observe(this, new Observer<List<ChatRoom>>() {
            @Override
            public void onChanged(List<ChatRoom> chatRooms) {
                if(chatRooms.size() > 0){
                    mChatsList.clear();
                    mChatsList.addAll(chatRooms);
                    Collections.sort(mChatsList);
                    mAdapter.notifyDataSetChanged();
                }else{
                    mNoChatsTV.setVisibility(View.VISIBLE);
                    mRv.setVisibility(View.GONE);

                }
                mChatsProgressView.setVisibility(View.GONE);
                mSwipeRefresh.setRefreshing(false);
            }
        });
    }

    private void setupRecyclerView() {
        mRv = mBinding.chatsRv;
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        mRv.setLayoutManager(manager);
        mAdapter = new ChatsAdapter(mChatsList, mMainModel.getNavController(), getContext());
        mRv.setAdapter(mAdapter);
        mRv.addItemDecoration(new DividerItemDecoration(mRv.getContext(),
                manager.getOrientation()));
    }

}