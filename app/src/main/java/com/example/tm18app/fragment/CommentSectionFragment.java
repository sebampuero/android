package com.example.tm18app.fragment;


import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;
import com.example.tm18app.adapters.CommentsAdapter;
import com.example.tm18app.databinding.FragmentCommentSectionBinding;
import com.example.tm18app.model.Comment;
import com.example.tm18app.viewModels.CommentsSectionViewModel;
import com.example.tm18app.viewModels.MyViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


/**
 * A simple {@link Fragment} subclass. Responsible for UI and events for the comment section UI.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class CommentSectionFragment extends BaseFragment {

    public static final String POST_ID = "postID";

    private FragmentCommentSectionBinding mBinding;
    private CommentsSectionViewModel mModel;
    private CommentsAdapter mAdapter;
    private List<Comment> mCommentsList = new ArrayList<>();
    private EditText mCommentInputEditText;
    private ProgressBar mProgressBar;
    private String mPostID;

    public CommentSectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mModel = ViewModelProviders.of(this).get(CommentsSectionViewModel.class);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_comment_section,
                container, false);
        mBinding.setLifecycleOwner(this);
        mBinding.setMyVM(mModel);
        mPostID = getArguments().getString(POST_ID);
        mModel.setPostID(mPostID);
        mModel.setAppContext(getActivity());
        setupViews();
        setupRecyclerView();
        fetchData();
        return mBinding.getRoot();
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        mCommentInputEditText = mBinding.commentInputField;
        mProgressBar = mBinding.progressBarComments;
    }

    /**
     * Starts the observer for the {@link androidx.lifecycle.LiveData} object that contains changes
     * of comments. When changes occur, they appear on screen.
     */
    private void fetchData() {
        mModel.getCommentLiveData().observe(this, comments -> {
            if(comments != null){
                HashSet<Comment> set = new HashSet<>(mCommentsList);
                set.addAll(comments);
                mCommentsList.clear();
                mCommentsList.addAll(set);
                Collections.sort(mCommentsList);
                mAdapter.notifyDataSetChanged();
                mCommentInputEditText.setText("");
            }else
                Toast.makeText(getContext(),
                        getString(R.string.cannot_load_comments), Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.GONE);
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
