package com.example.tm18app.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.CommentItemBinding;
import com.example.tm18app.fragment.OtherProfileFragment;
import com.example.tm18app.model.Comment;
import com.example.tm18app.repository.PostItemRepository;
import com.example.tm18app.util.DialogManager;
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

    private Context mContext;
    private ArrayList<Comment> mCommentsList;
    private int mCurrentUserId;
    private NavController mNavController;
    private SharedPreferences mPrefs;
    private int mProfilePicDimen;

    public CommentsAdapter(FragmentActivity activity, List<Comment> comments, NavController mNavController) {
        this.mContext = activity;
        this.mCommentsList = (ArrayList<Comment>) comments;
        mPrefs = mContext
                .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        this.mCurrentUserId = mPrefs.getInt(Constant.USER_ID, 0);
        this.mNavController = mNavController;
        mProfilePicDimen =  mContext.getResources().getInteger(R.integer.thumbnail_profile_pic);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CommentItemBinding binding = CommentItemBinding.inflate(inflater, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        Comment comment = mCommentsList.get(position);
        holder.name.setText(comment.getName());
        holder.lastname.setText(comment.getLastname());
        holder.content.setText(comment.getContent());
        holder.timestamp.setText(TimeUtils.parseTimestampToLocaleDatetime(comment.getTimestamp(), mContext));
        holder.commenterPic.setOnClickListener(view -> {
            int commentUserId = mCommentsList.get(position).getUserID();
            Bundle b = new Bundle();
            b.putString(OtherProfileFragment.OTHER_USER_ID, String.valueOf(commentUserId));
            mNavController
                    .navigate(R.id.action_commentSectionFragment_to_otherProfileFragment, b);
        });
        if(comment.getCommentatorPicUrl() != null)
            Picasso.get()
                    .load(comment.getCommentatorPicUrl())
                    .resize(mProfilePicDimen, mProfilePicDimen)
                    .centerCrop()
                    .into(holder.commenterPic);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return (mCommentsList != null) ? mCommentsList.size() : 0;
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
            binding.getRoot().setOnLongClickListener(view -> {
                showDeletDialog();
                return false;
            });
        }

        /**
         * Shows an alert dialog to the user to confirm the deletion of a comment. Upon acceptance,
         * the comment gets deleted.
         */
        private void showDeletDialog() {
            final int position = getAdapterPosition();
            final Comment commentToDelete = mCommentsList.get(position);
            if(mCurrentUserId == commentToDelete.getUserID()){
                DialogManager
                        .getInstance()
                        .showAlertDialogSingleButton(mContext,
                                mContext.getString(R.string.delete_comment_title),
                                mContext.getString(R.string.delete_comment_conf_message),
                                android.R.string.yes,
                                (dialogInterface, i) -> {
                                    PostItemRepository repository = new PostItemRepository();
                                    repository.deleteComment(commentToDelete.getId(),
                                            mPrefs.getString(Constant.PUSHY_TOKEN, ""));
                                    mCommentsList.remove(position);
                                    notifyItemRemoved(position);
                                });
            }
        }


    }
}
