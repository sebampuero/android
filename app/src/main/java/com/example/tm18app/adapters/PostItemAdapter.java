package com.example.tm18app.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.R;
import com.example.tm18app.databinding.PostCardviewBinding;
import com.example.tm18app.fragment.FeedFragment;
import com.example.tm18app.fragment.ProfileFragment;
import com.example.tm18app.pojos.Post;
import com.example.tm18app.util.TimeUtils;

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
                // Distinguish between feedfragment and profile fragment for nav controller to
                // correctly navigate to the comment section
                if(currentFragment instanceof FeedFragment)
                    navController.navigate(R.id.action_feedFragment_to_commentSectionFragment, bundle);
                else if(currentFragment instanceof ProfileFragment)
                    navController.navigate(R.id.action_profileFragment_to_commentSectionFragment, bundle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (postsList != null) ? postsList.size() : 0;
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nameLastname;
        TextView postTitle;
        TextView postContent;
        TextView goalTag;
        TextView commentCount;
        LinearLayout commentsSection;
        TextView timestamp;

        MyViewHolder(PostCardviewBinding binding) {
            super(binding.getRoot());

            nameLastname = binding.nameLastnameCardviewTv;
            postTitle = binding.postTitleCardviewTv;
            postContent = binding.postContentCardviewTv;
            goalTag = binding.goalTagTv;
            commentCount = binding.commentCountTv;
            commentsSection = binding.commentSectionLayout;
            timestamp = binding.timestamp;
        }


    }
}
