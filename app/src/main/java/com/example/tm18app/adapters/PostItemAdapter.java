package com.example.tm18app.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.PostCardviewBinding;
import com.example.tm18app.fragment.CommentSectionFragment;
import com.example.tm18app.fragment.FeedFragment;
import com.example.tm18app.fragment.OtherProfileFragment;
import com.example.tm18app.fragment.ProfileFragment;
import com.example.tm18app.fragment.PostWebViewFragment;
import com.example.tm18app.network.NetworkConnectivity;
import com.example.tm18app.model.Post;
import com.example.tm18app.repository.PostItemRepository;
import com.example.tm18app.util.TimeUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

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
    private ArrayList<Post> mPostsList;
    private NavController mNavController;
    private Fragment mCurrentFragment;
    private SharedPreferences mPrefs;
    private int profilePicDimen;

    public interface OnPostDeleteListener {

        /**
         * Listener method for when a Post is deleted
         * @param statusCode {@link MutableLiveData} that represents changes of the response's status
         *                                          code.
         */
        void onPostDeleted(MutableLiveData<Integer> statusCode);

    }

    public PostItemAdapter(ArrayList<Post> posts, NavController mNavController, Fragment fragment) {
        this.mPostsList = posts;
        this.mNavController = mNavController;
        this.mCurrentFragment = fragment;
        mPrefs = mCurrentFragment.getContext().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        profilePicDimen = fragment.getResources().getInteger(R.integer.thumbnail_profile_pic);
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
        final Post post = mPostsList.get(position);
        ArrayList<String> subscriberIds = null;
        if(post.getSubscriberIds() != null){
            subscriberIds = new ArrayList<>(Arrays.asList(post.getSubscriberIds().split(",")));
        }
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
                bundle.putString(CommentSectionFragment.POST_ID, String.valueOf(post.getId()));
                mNavController.navigate(R.id.commentSectionFragment, bundle);
            }
        });
        holder.moreVertOptions.setVisibility(View.GONE);
        holder.posterPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle b = new Bundle();
                b.putString(OtherProfileFragment.OTHER_USER_ID, String.valueOf(post.getUserID()));
                if(post.getUserID() != mPrefs.getInt(Constant.USER_ID, 0))
                    mNavController.navigate(R.id.otherProfileFragment, b);
                else
                    mNavController.navigate(R.id.profileFragment);
            }
        });
        if(subscriberIds != null){
            String userID = String.valueOf(mPrefs.getInt(Constant.USER_ID, 0));
            if(subscriberIds.indexOf(userID) >= 0){
                holder.moreVertOptions.setVisibility(View.VISIBLE);
                holder.moreVertOptions.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showOptionsAlertDialog(String.valueOf(post.getId()));
                    }
                });
            }
        }
        if(post.getPosterPicUrl() != null){
            holder.posterPicture.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(post.getPosterPicUrl()) // no need to tweak quality
                    .resize(profilePicDimen, profilePicDimen)
                    .centerCrop()
                    .into(holder.posterPicture);
        }else
            holder.posterPicture.setImageDrawable(mCurrentFragment.getResources().getDrawable(R.drawable.ic_person_black_24dp));
        if(post.getContentPicUrl() != null){
            holder.contentImage.setVisibility(View.VISIBLE); // known recyclerview / picasso bug
            // workaround for pictures not disappearing on scroll
            String imgUrl = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ? NetworkConnectivity // retrieve the image url to be downloaded by Picasso
                    .tweakImgQualityByNetworkType(mCurrentFragment.getContext(),
                            post.getContentPicUrl()) : post.getContentPicUrl();
            Picasso.get()
                    .load(imgUrl)
                    .placeholder(R.drawable.progress_img_animation)
                    .into(holder.contentImage);
        }else
            holder.contentImage.setVisibility(View.GONE);
    }

    private void showOptionsAlertDialog(final String postID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mCurrentFragment.getContext());
        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = mCurrentFragment.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.post_options_alert, null);
        dialog.setView(dialogLayout);
        dialog.show();
        dialog.setCancelable(true);
        dialogLayout.findViewById(R.id.unsubscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PostItemRepository repository = new PostItemRepository();
                repository.deleteSubscription(
                        String.valueOf(mPrefs.getInt(Constant.USER_ID, 0)),
                        postID,
                        mPrefs.getString(Constant.PUSHY_TOKEN, ""));
                Toast.makeText(
                        mCurrentFragment.getContext(),
                        mCurrentFragment.getResources().getString(R.string.unsubcribed_from_post),
                        Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    @Override
    public int getItemCount() {
        return (mPostsList != null) ? mPostsList.size() : 0;
    }


    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView nameLastname;
        TextView postTitle;
        TextView postContent;
        TextView goalTag;
        TextView commentCount;
        LinearLayout commentsSection;
        TextView timestamp;
        ImageView posterPicture;
        ImageView contentImage;
        ImageView moreVertOptions;

        MyViewHolder(final PostCardviewBinding binding) {
            super(binding.getRoot());

            nameLastname = binding.nameLastnameCardviewTv;
            postTitle = binding.postTitleCardviewTv;
            postContent = binding.postContentCardviewTv;
            goalTag = binding.goalTagTv;
            commentCount = binding.commentCountTv;
            commentsSection = binding.commentSectionLayout;
            timestamp = binding.timestamp;
            posterPicture = binding.posterPic;
            contentImage = binding.contentImage;
            moreVertOptions = binding.moreVertPost;

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
                    if(mPostsList.get(getAdapterPosition()).getContentPicUrl() != null)
                        openImage();
                }
            });
        }

        private void openImage() {
            Bundle bundle = new Bundle();
            bundle.putString(PostWebViewFragment.IMG_URL,
                    mPostsList.get(getAdapterPosition()).getContentPicUrl());
            bundle.putString(PostWebViewFragment.IMG_NAME,
                    String.valueOf(mPostsList.get(getAdapterPosition()).getId()));
            mNavController.navigate(R.id.postImgWebviewFragment, bundle);
        }

        /**
         * Shows an {@link AlertDialog} to the user to confirm the deletion of a Post. Upon acceptance,
         * the post gets deleted.
         */
        private void buildDeletionAlertDialog() {
            int userID = mPrefs.getInt(Constant.USER_ID, 0);
            final int position = getAdapterPosition();
            final Post postToDelete = mPostsList.get(position);
            if(userID == postToDelete.getUserID()){
                final MutableLiveData<Integer> statusCodeLiveData = new MutableLiveData<>();
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mCurrentFragment.getContext());
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle(mCurrentFragment.getContext().getString(R.string.delete_post_dialog_title));
                alertBuilder.setMessage(mCurrentFragment.getContext().getString(R.string.delete_post_conf_message));
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        PostItemRepository repository = new PostItemRepository();
                        repository.deletePost(postToDelete.getId(),
                                statusCodeLiveData,
                                mPrefs.getString(Constant.PUSHY_TOKEN, ""));
                        if(mCurrentFragment instanceof FeedFragment){
                            FeedFragment feed = (FeedFragment) mCurrentFragment;
                            feed.onPostDeleted(statusCodeLiveData);
                        }else if(mCurrentFragment instanceof ProfileFragment){
                            ProfileFragment profile = (ProfileFragment) mCurrentFragment;
                            profile.onPostDeleted(statusCodeLiveData);
                        }
                        mPostsList.remove(position);
                        notifyItemRemoved(position);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
            }
        }

    }
}
