package com.example.tm18app.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.PostCardviewBinding;
import com.example.tm18app.fragment.FeedFragment;
import com.example.tm18app.fragment.ProfileFragment;
import com.example.tm18app.fragment.WebviewFragment;
import com.example.tm18app.pojos.Post;
import com.example.tm18app.repository.PostItemRepository;
import com.example.tm18app.util.TimeUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Adapter for the post items in Feed and Profile
 * @see com.example.tm18app.fragment.FeedFragment
 * @see com.example.tm18app.fragment.ProfileFragment
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class PostItemAdapter extends RecyclerView.Adapter<PostItemAdapter.MyViewHolder> {
    private ArrayList<Post> postsList;
    private NavController navController;
    private Fragment currentFragment;

    public interface OnPostDeleteListener {

        /**
         * Listener method for when a Post is deleted
         * @param statusCode {@link MutableLiveData} that represents changes of the response's status
         *                                          code.
         */
        void onPostDeleted(MutableLiveData<Integer> statusCode);

    }

    public PostItemAdapter(ArrayList<Post> posts, NavController navController, Fragment fragment) {
        this.postsList = posts;
        this.navController = navController;
        this.currentFragment = fragment;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        PostCardviewBinding itemBinding = PostCardviewBinding.inflate(layoutInflater, parent, false);
        return new MyViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final Post post = postsList.get(position);
        holder.nameLastname.setText(String.format("%s %s", post.getName(), post.getLastname()));
        holder.postTitle.setText(post.getTitle());
        holder.postContent.setText(post.getContent());
        holder.goalTag.setText(post.getGoalTag());
        holder.commentCount.setText(String.valueOf(post.getCommentCount()));
        holder.timestamp.setText(TimeUtils.parseTimestampToLocaleDatetime(post.getTimestamp()));
        holder.commentsSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("postID", String.valueOf(post.getId()));
                // Distinguish between feed fragment and profile fragment for nav controller to
                // correctly navigate to the comment section
                if(currentFragment instanceof FeedFragment)
                    navController.navigate(R.id.action_feedFragment_to_commentSectionFragment, bundle);
                else if(currentFragment instanceof ProfileFragment)
                    navController.navigate(R.id.action_profileFragment_to_commentSectionFragment, bundle);
            }
        });
        if(post.getPosterPicUrl() != null){
            if(!post.getPosterPicUrl().equals(""))
                Picasso.get().load(post.getPosterPicUrl()) //TODO: use dimens values
                        .resize(70, 70).centerCrop().into(holder.posterPicUrl);
            // else , set drawable as default for person black
        }
        if(post.getContentPicUrl() != null){
            if(!post.getContentPicUrl().equals("")){
                Picasso.get().load(post.getContentPicUrl()) //TODO: use dimens values
                        .resize(0, 500).into(holder.contentImage);
            }else{
                holder.contentImage.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return (postsList != null) ? postsList.size() : 0;
    }


    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView nameLastname;
        TextView postTitle;
        TextView postContent;
        TextView goalTag;
        TextView commentCount;
        LinearLayout commentsSection;
        TextView timestamp;
        ImageView posterPicUrl;
        ImageView contentImage;

        MyViewHolder(final PostCardviewBinding binding) {
            super(binding.getRoot());

            nameLastname = binding.nameLastnameCardviewTv;
            postTitle = binding.postTitleCardviewTv;
            postContent = binding.postContentCardviewTv;
            goalTag = binding.goalTagTv;
            commentCount = binding.commentCountTv;
            commentsSection = binding.commentSectionLayout;
            timestamp = binding.timestamp;
            posterPicUrl = binding.posterPic;
            contentImage = binding.contentImage;

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
                    if(postsList.get(getAdapterPosition()).getContentPicUrl() != null)
                        buildDialogToOpenWV();
                }
            });
        }

        private void buildDialogToOpenWV() {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(currentFragment.getContext());
            alertBuilder.setCancelable(true);
            alertBuilder.setTitle(currentFragment.getContext().getString(R.string.open_img_alert_title));
            alertBuilder.setMessage(currentFragment.getContext().getString(R.string.open_img_alert_text));
            alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    Bundle bundle = new Bundle();
                    bundle.putString(WebviewFragment.IMG_URL,
                            postsList.get(getAdapterPosition()).getContentPicUrl());
                    if(currentFragment instanceof FeedFragment)
                        navController.navigate(R.id.action_feedFragment_to_webviewFragment, bundle);
                    else if(currentFragment instanceof ProfileFragment)
                        navController.navigate(R.id.action_profileFragment_to_webviewFragment, bundle);
                }
            });
            AlertDialog alert = alertBuilder.create();
            alert.show();
        }

        /**
         * Shows an alert dialog to the user to confirm the deletion of a Post. Upon acceptance,
         * the post gets deleted.
         */
        private void buildDeletionAlertDialog() {
            SharedPreferences preferences = currentFragment.getContext()
                    .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
            int userID = preferences.getInt(Constant.USER_ID, 0);
            final int position = getAdapterPosition();
            final Post postToDelete = postsList.get(position);
            if(userID == postToDelete.getUserID()){
                final MutableLiveData<Integer> statusCode = new MutableLiveData<>();
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(currentFragment.getContext());
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle(currentFragment.getContext().getString(R.string.delete_post_dialog_title));
                alertBuilder.setMessage(currentFragment.getContext().getString(R.string.delete_post_conf_message));
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        PostItemRepository repository = new PostItemRepository();
                        repository.deletePost(postToDelete.getId(), statusCode);
                        if(currentFragment instanceof FeedFragment){
                            FeedFragment feed = (FeedFragment) currentFragment;
                            feed.onPostDeleted(statusCode);
                        }else if(currentFragment instanceof ProfileFragment){
                            ProfileFragment profile = (ProfileFragment) currentFragment;
                            profile.onPostDeleted(statusCode);
                        }
                        postsList.remove(position);
                        notifyItemRemoved(position);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
            }
        }

    }
}
