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
import android.widget.EditText;

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
 * A simple {@link Fragment} subclass. Responsible for UI and events for the comment section UI.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class CommentSectionFragment extends Fragment {

    public static final String POST_ID = "postID";

    private FragmentCommentSectionBinding binding;
    private CommentsSectionViewModel model;
    private MyViewModel mainModel;
    private CommentsAdapter adapter;
    private List<Comment> commentsList = new ArrayList<>();
    private EditText input;

    public CommentSectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        model = ViewModelProviders.of(getActivity()).get(CommentsSectionViewModel.class);
        mainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_comment_section, container, false);
        binding.setLifecycleOwner(this);
        binding.setMyVM(model);
        model.setPostID(getArguments().getString(POST_ID));
        model.setAppContext(getActivity());
        input = binding.commentInputField;
        setupRecyclerView();
        fetchData();
        return binding.getRoot();
    }

    /**
     * Starts the observer for the {@link androidx.lifecycle.LiveData} object that contains changes
     * of comments. When changes occur, they appear on screen.
     */
    private void fetchData() {
        model.getCommentLiveData().observe(this, new Observer<List<Comment>>() {
            @Override
            public void onChanged(List<Comment> comments) {
                if(comments != null){
                    commentsList.clear();
                    commentsList.addAll(comments);
                    // sort comments by creation date
                    Collections.sort(commentsList);
                    adapter.notifyDataSetChanged();
                    input.setText("");
                }
            }
        });
    }

    /**
     * Sets up the {@link RecyclerView} for this {@link Fragment}
     */
    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.commentsRv;
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        adapter = new CommentsAdapter(getActivity(), commentsList, mainModel.getNavController());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                manager.getOrientation()));
    }

}
