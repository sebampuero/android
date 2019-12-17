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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;
import com.example.tm18app.adapters.ChatsAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.FragmentChatsBinding;
import com.example.tm18app.model.Chat;
import com.example.tm18app.viewModels.ChatsViewModel;
import com.example.tm18app.viewModels.MyViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private FragmentChatsBinding mBinding;
    private ChatsViewModel mModel;
    private MyViewModel mMainModel;
    private ChatsAdapter mAdapter;
    private List<Chat> mChatsList = new ArrayList<>();

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        mModel = ViewModelProviders.of(getActivity()).get(ChatsViewModel.class);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_chats,container, false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        SharedPreferences prefs = getActivity()
                .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        mModel.setPrefs(prefs);
        setupViews();
        setupRecyclerView();
        fetchData();
        return mBinding.getRoot();
    }

    private void setupViews() {
        Toolbar toolbar = ((MainActivity)getActivity()).getToolbar();
        toolbar.getMenu().clear();
    }

    private void fetchData() {
        mModel.callRepository();
        mModel.getChatLiveData().observe(this, new Observer<List<Chat>>() {
            @Override
            public void onChanged(List<Chat> chats) {
                if(chats.size() > 0){
                    mChatsList.clear();
                    mChatsList.addAll(chats);
                    Collections.sort(mChatsList);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void setupRecyclerView() {
        RecyclerView rv = mBinding.chatsRv;
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        rv.setLayoutManager(manager);
        mAdapter = new ChatsAdapter(mChatsList, mMainModel.getNavController());
        rv.setAdapter(mAdapter);
        rv.addItemDecoration(new DividerItemDecoration(rv.getContext(),
                manager.getOrientation()));
    }

}
