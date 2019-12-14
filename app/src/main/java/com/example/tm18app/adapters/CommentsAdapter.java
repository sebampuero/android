package com.example.tm18app.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.CommentItemBinding;
import com.example.tm18app.fragment.OtherProfileFragment;
import com.example.tm18app.pojos.Comment;
import com.example.tm18app.repository.PostItemRepository;
import com.example.tm18app.util.TimeUtils;
import com.squareup.picasso.Picasso;

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
    private int currentUserId;
    private NavController navController;

    public CommentsAdapter(FragmentActivity activity, List<Comment> comments, NavController navController) {
        this.appContext = activity;
        this.commentsList = (ArrayList<Comment>) comments;
        SharedPreferences preferences = appContext
                .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        this.currentUserId = preferences.getInt(Constant.USER_ID, 0);
        this.navController = navController;
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
        // TODO: use dimens values
        if(comment.getCommentatorPicUrl() != null)
            if(!comment.getCommentatorPicUrl().equals("")) // get image from cloudinary
                Picasso.get().load(comment.getCommentatorPicUrl())
                        .resize(50,50).centerCrop().into(holder.commenterPic);
            // load default drawable if no image set
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
        ImageView commenterPic;

        MyViewHolder(CommentItemBinding binding) {
            super(binding.getRoot());
            name = binding.name;
            lastname = binding.lastname;
            content = binding.commentContent;
            timestamp = binding.timestamp;
            commenterPic = binding.commenterPic;
            binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    buildDeletionAlertDialog();
                    return false;
                }
            });

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    buildOpenProfileAlertDialog();
                }
            });
        }

        private void buildOpenProfileAlertDialog() {
            Comment comment = commentsList.get(getAdapterPosition());
            if(comment.getUserID() != currentUserId){
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(appContext);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle(appContext.getString(R.string.open_profile_alert_title));
                alertBuilder.setMessage(appContext.getString(R.string.open_profile_alert_text) + " " + comment.getName() +" ?");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        int commentUserId = commentsList.get(getAdapterPosition()).getUserID();
                        Bundle b = new Bundle();
                        b.putString(OtherProfileFragment.OTHER_USER_ID, String.valueOf(commentUserId));
                        navController
                                .navigate(R.id.action_commentSectionFragment_to_otherProfileFragment, b);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
            }
        }

        /**
         * Shows an alert dialog to the user to confirm the deletion of a comment. Upon acceptance,
         * the comment gets deleted.
         */
        private void buildDeletionAlertDialog() {
            final int position = getAdapterPosition();
            final Comment commentToDelete = commentsList.get(position);
            if(currentUserId == commentToDelete.getUserID()){
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(appContext);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle(appContext.getString(R.string.delete_comment_title));
                alertBuilder.setMessage(appContext.getString(R.string.delete_comment_conf_message));
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        PostItemRepository repository = new PostItemRepository();
                        repository.deleteComment(commentToDelete.getId());
                        commentsList.remove(position);
                        notifyItemRemoved(position);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
            }
        }


    }
}
