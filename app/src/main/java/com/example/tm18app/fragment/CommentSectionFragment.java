package com.example.tm18app.fragment;


import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tm18app.R;
import com.example.tm18app.adapters.CommentsAdapter;
import com.example.tm18app.databinding.FragmentCommentSectionBinding;
import com.example.tm18app.pojos.Comment;
import com.example.tm18app.viewModels.CommentsSectionViewModel;
import com.example.tm18app.viewModels.MyViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class CommentSectionFragment extends Fragment {

    private FragmentCommentSectionBinding binding;
    private CommentsSectionViewModel model;
    private RecyclerView recyclerView;
    private CommentsAdapter adapter;
    private List<Comment> commentsList = new ArrayList<>();

    public CommentSectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        model = ViewModelProviders.of(getActivity()).get(CommentsSectionViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_comment_section, container, false);
        binding.setLifecycleOwner(this);
        binding.setMyVM(model);
        model.setPostID(getArguments().getString("postID"));
        model.setAppContext(getActivity());
        setupRecyclerView();
        fetchData();
        return binding.getRoot();
    }

    private void fetchData() {
        model.getCommentLiveData().observe(this, new Observer<List<Comment>>() {
            @Override
            public void onChanged(List<Comment> comments) { ;
                if(comments != null){
                    commentsList.clear();
                    commentsList.addAll(comments);
                    Collections.sort(commentsList);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }


    private void setupRecyclerView() {
        recyclerView = binding.commentsRv;
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        adapter = new CommentsAdapter(getActivity(), commentsList);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                manager.getOrientation()));
    }

}
