package com.example.tm18app.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
    private HashMap<Integer, SimpleExoPlayer> videoPlayers;

    /**
     * Listener for posts deletes
     */
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
        videoPlayers = new HashMap<>();
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
        holder.onBind(post);
    }

    @Override
    public int getItemCount() {
        return (mPostsList != null) ? mPostsList.size() : 0;
    }

    /**
     * Releases all instantiated {@link com.google.android.exoplayer2.ExoPlayer} inside the
     * {@link HashMap}. Useful for when the App is closed and memory has to be released and therefore
     * prevent memory leaks.
     */
    public void releasePlayers() {
        if(videoPlayers != null)
            if(!videoPlayers.isEmpty()){
                for(SimpleExoPlayer player : videoPlayers.values()){
                    player.release();
                }
            }
    }

    /**
     * Pauses all instantiated {@link com.google.android.exoplayer2.ExoPlayer} inside the {@link HashMap}
     */
    public void pausePlayers() {
        if(videoPlayers != null)
            if(!videoPlayers.isEmpty()){
                for(SimpleExoPlayer player : videoPlayers.values()){
                    player.setPlayWhenReady(!player.getPlayWhenReady());
                }
            }
    }


    /**
     * Custom {@link androidx.recyclerview.widget.RecyclerView.ViewHolder} subclass.
     */
    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView nameLastname;
        TextView postTitle;
        TextView postContent;
        TextView goalTag;
        TextView commentCount;
        LinearLayout commentsSection;
        TextView timestamp;
        ImageView posterProfilePic;
        ImageView contentImage;
        ImageView moreVertOptions;
        ProgressBar progressBarVideo;
        PlayerView surfaceView;

        MyViewHolder(final PostCardviewBinding binding) {
            super(binding.getRoot());

            nameLastname = binding.nameLastnameCardviewTv;
            postTitle = binding.postTitleCardviewTv;
            postContent = binding.postContentCardviewTv;
            goalTag = binding.goalTagTv;
            commentCount = binding.commentCountTv;
            commentsSection = binding.commentSectionLayout;
            timestamp = binding.timestamp;
            posterProfilePic = binding.posterPic;
            contentImage = binding.contentImage;
            moreVertOptions = binding.moreVertPost;
            progressBarVideo = binding.progressBarVideo;
            surfaceView = binding.videoPlayerPost;

            binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    buildDeletionAlertDialog();
                    return false;
                }
            });

        }


        /**
         * Binds a {@link Post} into the {@link androidx.recyclerview.widget.RecyclerView.ViewHolder}
         * @param post {@link Post}
         */
        public void onBind(final Post post) {
            ArrayList<String> subscriberIds = null;
            if(post.getSubscriberIds() != null){
                // subscriber ids comes in a csv format
                subscriberIds = new ArrayList<>(Arrays.asList(post.getSubscriberIds().split(",")));
            }
            nameLastname.setText(String.format("%s %s", post.getName(), post.getLastname()));
            postTitle.setText(post.getTitle());
            postContent.setText(post.getContent());
            goalTag.setText(post.getGoalTag());
            commentCount.setText(String.valueOf(post.getCommentCount()));
            timestamp.setText(TimeUtils.parseTimestampToLocaleDatetime(post.getTimestamp()));
            commentsSection.setOnClickListener(new CommentsClickListener());
            moreVertOptions.setVisibility(View.GONE);
            posterProfilePic.setOnClickListener(new ProfilePicClickListener());
            if(subscriberIds != null){
                String userID = String.valueOf(mPrefs.getInt(Constant.USER_ID, 0));
                // if the current user is inside this post's subscribers, show options accordingly
                if(subscriberIds.indexOf(userID) >= 0){
                    moreVertOptions.setVisibility(View.VISIBLE);
                    moreVertOptions.setOnClickListener(new SubscriberOptionsClickListener());
                }
            }
            if(post.getPosterPicUrl() != null){
                posterProfilePic.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(post.getPosterPicUrl()) // no need to tweak quality
                        .resize(profilePicDimen, profilePicDimen)
                        .centerCrop()
                        .into(posterProfilePic);
            }else
                posterProfilePic.setImageDrawable(mCurrentFragment
                        .getResources()
                        .getDrawable(R.drawable.ic_person_black_24dp));
            if(post.getContentPicUrl() != null){
                contentImage.setVisibility(View.VISIBLE);
                surfaceView.setVisibility(View.GONE); // hide the video player view is visible
                // needed because recyclerview recycles views and sometimes the recycled view appears
                // on other posts that have no video media set
                String imgUrl =  NetworkConnectivity
                        .tweakImgQualityByNetworkType(mCurrentFragment.getContext(),
                                post.getContentPicUrl());
                Picasso.get()
                        .load(imgUrl)
                        .placeholder(R.drawable.progress_img_animation)
                        .into(contentImage);
                contentImage.setOnClickListener(new ImageClickListener());
            }else{
                contentImage.setVisibility(View.GONE);
                surfaceView.setVisibility(View.GONE);
            }
            if(post.getContentVideoUrl() != null){
                contentImage.setVisibility(View.VISIBLE); // for thumbnail
                Picasso.get()
                        .load(R.drawable.thumbnail_video)
                        .into(contentImage);
                contentImage.setOnClickListener(new VideoThumbnailClickListener());
            }else{
                surfaceView.setVisibility(View.GONE);
            }
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
                        // releases the video corresponding to this post if available to release memory
                        if(videoPlayers.get(getAdapterPosition()) != null)
                            videoPlayers.get(getAdapterPosition()).release();
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

        /**
         * Shows a {@link AlertDialog} for when the subscriber options view is clicked.
         * @param postID {@link Integer}
         */
        private void showSubscriberOptionsAlertDialog(final String postID) {
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

        /**
         * Subscriber options click listener.
         */
        class SubscriberOptionsClickListener implements View.OnClickListener {

            @Override
            public void onClick(View view) {
                Post post = mPostsList.get(getAdapterPosition());
                showSubscriberOptionsAlertDialog(String.valueOf(post.getId()));
            }
        }

        /**
         * Comments click listener.
         */
        class CommentsClickListener implements View.OnClickListener {

            @Override
            public void onClick(View view) {
                Post post = mPostsList.get(getAdapterPosition());
                Bundle bundle = new Bundle();
                bundle.putString(CommentSectionFragment.POST_ID, String.valueOf(post.getId()));
                mNavController.navigate(R.id.commentSectionFragment, bundle);
            }
        }

        /**
         * Profile picture click listener.
         */
        class ProfilePicClickListener implements View.OnClickListener {

            @Override
            public void onClick(View view) {
                Post post = mPostsList.get(getAdapterPosition());
                Bundle b = new Bundle();
                b.putString(OtherProfileFragment.OTHER_USER_ID, String.valueOf(post.getUserID()));
                if(post.getUserID() != mPrefs.getInt(Constant.USER_ID, 0))
                    mNavController.navigate(R.id.otherProfileFragment, b);
                else
                    mNavController.navigate(R.id.profileFragment);
            }
        }

        /**
         * Content image click listener.
         */
        class ImageClickListener implements View.OnClickListener {

            @Override
            public void onClick(View view) {
                Post post = mPostsList.get(getAdapterPosition());
                Bundle bundle = new Bundle();
                bundle.putString(PostWebViewFragment.IMG_URL,
                        post.getContentPicUrl());
                bundle.putString(PostWebViewFragment.IMG_NAME,
                        String.valueOf(post.getId()));
                mNavController.navigate(R.id.postImgWebviewFragment, bundle);
            }
        }

        /**
         * Videothumbnail click listener.
         */
        class VideoThumbnailClickListener implements View.OnClickListener {

            @Override
            public void onClick(View view) {
                //Init video player params
                BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                TrackSelection.Factory videoTrackSelectionFactory =
                        new AdaptiveTrackSelection.Factory(bandwidthMeter);
                TrackSelector trackSelector =
                        new DefaultTrackSelector(videoTrackSelectionFactory);
                SimpleExoPlayer videoPlayer = ExoPlayerFactory
                        .newSimpleInstance(mCurrentFragment.getContext(), trackSelector);
                videoPlayers.put(getAdapterPosition(), videoPlayer);
                surfaceView.setUseController(true); // allow user to pause/resume video
                surfaceView.setPlayer(videoPlayer);
                videoPlayer.addListener(new PlayerListener());
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                        mCurrentFragment.getContext(),
                        Util.getUserAgent(mCurrentFragment.getContext(), "RecyclerView VideoPlayer"));
                String contentUrl = mPostsList.get(getAdapterPosition()).getContentVideoUrl();
                if(contentUrl != null){
                    MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(Uri.parse(contentUrl));
                    videoPlayer.prepare(videoSource);
                    videoPlayer.setPlayWhenReady(true);
                }
            }
        }

        /**
         * Videoplayer events listener.
         */
        class PlayerListener implements Player.EventListener {

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        progressBarVideo.setVisibility(View.VISIBLE);
                        contentImage.setVisibility(View.GONE);
                        break;
                    case Player.STATE_READY:
                        progressBarVideo.setVisibility(View.GONE);
                        surfaceView.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        }
    }
}
