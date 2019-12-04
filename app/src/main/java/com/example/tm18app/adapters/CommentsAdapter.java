package com.example.tm18app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.databinding.CommentItemBinding;
import com.example.tm18app.pojos.Comment;
import com.example.tm18app.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for comments section
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class CommentsAdapter  extends RecyclerView.Adapter<CommentsAdapter.MyViewHolder> {

    private Context appContext;
    private ArrayList<Comment> commentsList;

    public CommentsAdapter(FragmentActivity activity, List<Comment> comments) {
        this.appContext = activity;
        this.commentsList = (ArrayList<Comment>) comments;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CommentItemBinding binding = CommentItemBinding.inflate(inflater, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Comment comment = commentsList.get(position);
        holder.name.setText(comment.getName());
        holder.lastname.setText(comment.getLastname());
        holder.content.setText(comment.getContent());
        holder.timestamp.setText(TimeUtils.parseTimestampToLocaleDatetime(comment.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return (commentsList != null) ? commentsList.size() : 0;
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView lastname;
        TextView content;
        TextView timestamp;

        MyViewHolder(CommentItemBinding binding) {
            super(binding.getRoot());

            name = binding.name;
            lastname = binding.lastname;
            content = binding.commentContent;
            timestamp = binding.timestamp;
        }


    }
}
