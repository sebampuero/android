package com.example.tm18app.fragment;


import android.os.Bundle;

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

    private FragmentCommentSectionBinding mBinding;
    private CommentsSectionViewModel mModel;
    private MyViewModel mMainModel;
    private CommentsAdapter mAdapter;
    private List<Comment> mCommentsList = new ArrayList<>();
    private EditText mCommentInputEditText;

    public CommentSectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mModel = ViewModelProviders.of(getActivity()).get(CommentsSectionViewModel.class);
        mMainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_comment_section, container, false);
        mBinding.setLifecycleOwner(this);
        mBinding.setMyVM(mModel);
        mModel.setPostID(getArguments().getString(POST_ID));
        mModel.setAppContext(getActivity());
        mCommentInputEditText = mBinding.commentInputField;
        setupRecyclerView();
        fetchData();
        return mBinding.getRoot();
    }

    /**
     * Starts the observer for the {@link androidx.lifecycle.LiveData} object that contains changes
     * of comments. When changes occur, they appear on screen.
     */
    private void fetchData() {
        mModel.getCommentLiveData().observe(this, new Observer<List<Comment>>() {
            @Override
            public void onChanged(List<Comment> comments) {
                if(comments != null){
                    mCommentsList.clear();
                    mCommentsList.addAll(comments);
                    // sort comments by creation date
                    Collections.sort(mCommentsList);
                    mAdapter.notifyDataSetChanged();
                    mCommentInputEditText.setText("");
                }
            }
        });
    }

    /**
     * Sets up the {@link RecyclerView} for this {@link Fragment}
     */
    private void setupRecyclerView() {
        RecyclerView recyclerView = mBinding.commentsRv;
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        mAdapter = new CommentsAdapter(getActivity(), mCommentsList, mMainModel.getNavController());
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                manager.getOrientation()));
    }

}
