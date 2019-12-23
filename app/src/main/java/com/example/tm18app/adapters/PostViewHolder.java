package com.example.tm18app.adapters;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.tm18app.fragment.PostWebViewFragment;
import com.example.tm18app.fragment.ProfileFragment;
import com.example.tm18app.model.Post;
import com.example.tm18app.network.NetworkConnectivity;
import com.example.tm18app.repository.PostItemRepository;
import com.example.tm18app.util.TimeUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostViewHolder extends RecyclerView.ViewHolder{

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
    FrameLayout mediaContainer;
    ImageView thumbnail;
    ProgressBar progressBar;
    View parent;
    Fragment mCurrentFragment;
    ArrayList<Post> mPostsList;
    NavController mNavController;
    SharedPreferences mPrefs;
    PostItemAdapter postItemAdapter;
    int profilePicDimen;


    public PostViewHolder(final PostCardviewBinding binding, Fragment fragment, ArrayList<Post> posts, NavController nav, SharedPreferences prefs, PostItemAdapter adapter) {
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
        mediaContainer = binding.mediaContainer;
        thumbnail = binding.thumbnail;
        progressBar = binding.progressBarVideo;
        parent = binding.getRoot();
        mCurrentFragment = fragment;
        mPostsList = posts;
        mNavController = nav;
        mPrefs = prefs;
        postItemAdapter = adapter;
        profilePicDimen = fragment.getResources().getInteger(R.integer.thumbnail_profile_pic);

        binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                buildDeletionAlertDialog();
                return false;
            }
        });

        binding.getRoot()
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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
                    postItemAdapter.notifyItemRemoved(position);
                }
            });
            AlertDialog alert = alertBuilder.create();
            alert.show();
        }
    }

    public void onBind(final Post post) {
        parent.setTag(this);
        ArrayList<String> subscriberIds = null;
        if(post.getSubscriberIds() != null){
            subscriberIds = new ArrayList<>(Arrays.asList(post.getSubscriberIds().split(",")));
        }
        nameLastname.setText(String.format("%s %s", post.getName(), post.getLastname()));
        postTitle.setText(post.getTitle());
        postContent.setText(post.getContent());
        goalTag.setText(post.getGoalTag());
        commentCount.setText(String.valueOf(post.getCommentCount()));
        timestamp.setText(TimeUtils.parseTimestampToLocaleDatetime(post.getTimestamp()));
        commentsSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(CommentSectionFragment.POST_ID, String.valueOf(post.getId()));
                mNavController.navigate(R.id.commentSectionFragment, bundle);
            }
        });
        moreVertOptions.setVisibility(View.GONE);
        posterPicture.setOnClickListener(new View.OnClickListener() {
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
                moreVertOptions.setVisibility(View.VISIBLE);
                moreVertOptions.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showOptionsAlertDialog(String.valueOf(post.getId()));
                    }
                });
            }
        }
        if(post.getPosterPicUrl() != null){
            posterPicture.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(post.getPosterPicUrl()) // no need to tweak quality
                    .resize(profilePicDimen, profilePicDimen)
                    .centerCrop()
                    .into(posterPicture);
        }else
            posterPicture.setImageDrawable(mCurrentFragment.getResources().getDrawable(R.drawable.ic_person_black_24dp));

        if(post.getContentPicUrl() != null){
            thumbnail.setVisibility(View.GONE);
            contentImage.setVisibility(View.VISIBLE);
            // workaround for pictures not disappearing on scroll
            String imgUrl = NetworkConnectivity // retrieve the image url to be downloaded by Picasso
                    .tweakImgQualityByNetworkType(mCurrentFragment.getContext(),
                            post.getContentPicUrl());
            Picasso.get()
                    .load(imgUrl)
                    .placeholder(R.drawable.progress_img_animation)
                    .into(contentImage);
        }else{
            contentImage.setVisibility(View.GONE);
            thumbnail.setVisibility(View.GONE);
        }

        if(post.getContentVideoUrl() != null){
            thumbnail.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(R.drawable.thumbnail_video)
                    .into(thumbnail);
        }else
            thumbnail.setVisibility(View.GONE);
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
}
