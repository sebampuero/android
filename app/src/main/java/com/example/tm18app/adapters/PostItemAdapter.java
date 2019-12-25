package com.example.tm18app.adapters;

import android.app.Activity;
import android.content.Context;
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
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.PostCardviewBinding;
import com.example.tm18app.fragment.CommentSectionFragment;
import com.example.tm18app.fragment.OtherProfileFragment;
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
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
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
    private Context mContext;
    private SharedPreferences mPrefs;
    private int profilePicDimen;
    private HashMap<Integer, SimpleExoPlayer> videoPlayers;
    private Post recentlyDeletedPost;
    private int recentlyDeletedPostPosition;
    private PostsEventsListener postsEventsListener;

    private final String TAG = getClass().getSimpleName();

    /**
     * Listener for posts deletes
     */
    public interface PostsEventsListener {

        /**
         * Called when a {@link Post} is deleted
         * @param statusCode {@link MutableLiveData} that represents changes of the response's status
         *                                          code.
         */
        default void onPostDeleted(MutableLiveData<Integer> statusCode) {}

        /**
         * Called when a {@link Post} was deleted but the action was undone
         * @param itemPosition {@link Integer} position in array of the deleted {@link Post}
         */
        default void onUndoPostDeleted(int itemPosition) {}

        void onPlayerReproducing(boolean reproducing);

    }

    public PostItemAdapter(ArrayList<Post> posts, NavController mNavController, Context context, PostsEventsListener postsEventsListener) {
        this.mPostsList = posts;
        this.mNavController = mNavController;
        this.mContext = context;
        mPrefs = context.getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        profilePicDimen = context.getResources().getInteger(R.integer.thumbnail_profile_pic);
        videoPlayers = new HashMap<>();
        this.postsEventsListener = postsEventsListener;
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
                    player.setPlayWhenReady(false);
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
            posterProfilePic.setOnClickListener(new ProfilePicClickListener());
            moreVertOptions.setOnClickListener(new OptionsClickListener(subscriberIds));
            if(post.getPosterPicUrl() != null){
                posterProfilePic.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(post.getPosterPicUrl()) // no need to tweak quality
                        .resize(profilePicDimen, profilePicDimen)
                        .centerCrop()
                        .into(posterProfilePic);
            }else
                posterProfilePic.setImageDrawable(mContext
                        .getResources()
                        .getDrawable(R.drawable.ic_person_black_24dp));
            if(post.getContentPicUrl() != null){
                contentImage.setVisibility(View.VISIBLE);
                surfaceView.setVisibility(View.GONE); // hide the video player view is visible
                // needed because recyclerview recycles views and sometimes the recycled view appears
                // on other posts that have no video media set
                String imgUrl =  NetworkConnectivity
                        .tweakImgQualityByNetworkType(mContext,
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
         * Comments click postsEventsListener.
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
         * Profile picture click postsEventsListener.
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
         * Content image click postsEventsListener.
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
         * Videothumbnail click postsEventsListener.
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
                        .newSimpleInstance(mContext, trackSelector);
                // if there is already a videoplayer instance in the position index, release it to
                // not occupy too much memory
                if(videoPlayers.get(getAdapterPosition()) != null)
                    videoPlayers.get(getAdapterPosition()).release();
                videoPlayers.put(getAdapterPosition(), videoPlayer);
                surfaceView.setUseController(true); // allow user to pause/resume video
                surfaceView.setPlayer(videoPlayer);
                videoPlayer.addListener(new PlayerListener());
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                        mContext,
                        Util.getUserAgent(mContext, "RecyclerView VideoPlayer"));
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
         * Videoplayer events postsEventsListener.
         */
        class PlayerListener implements Player.EventListener {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                postsEventsListener.onPlayerReproducing(isPlaying);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        progressBarVideo.setVisibility(View.VISIBLE);
                        contentImage.setVisibility(View.GONE);
                        postsEventsListener.onPlayerReproducing(true);
                        break;
                    case Player.STATE_READY:
                        progressBarVideo.setVisibility(View.GONE);
                        surfaceView.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }
        }

        /**
         * Post options click postsEventsListener
         */
        class OptionsClickListener implements View.OnClickListener {

            ArrayList<String> subscriberIds;

            OptionsClickListener(ArrayList<String> subscriberIds){
                this.subscriberIds = subscriberIds;
            }

            @Override
            public void onClick(View view) {
                final int userID = mPrefs.getInt(Constant.USER_ID, 0);
                final Post post = mPostsList.get(getAdapterPosition());
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                final AlertDialog dialog = builder.create();
                LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.post_options_alert, null);
                dialog.setView(dialogLayout);
                dialog.show();
                dialog.setCancelable(true);
                dialogLayout.findViewById(R.id.reportPost).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(mContext,
                                "Not yet implemented!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                if(subscriberIds != null){
                    if(subscriberIds.indexOf(String.valueOf(userID)) >= 0) {
                        dialogLayout.findViewById(R.id.unsubscribe).setVisibility(View.VISIBLE);
                        dialogLayout.findViewById(R.id.unsubscribe).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                PostItemRepository repository = new PostItemRepository();
                                repository.deleteSubscription(
                                        String.valueOf(userID),
                                        String.valueOf(post.getId()),
                                        mPrefs.getString(Constant.PUSHY_TOKEN, ""));
                                Toast.makeText(
                                        mContext,
                                        mContext.getResources().getString(R.string.unsubcribed_from_post),
                                        Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                    }
                }
                if(post.getUserID() == userID){
                    dialogLayout.findViewById(R.id.deletePost).setVisibility(View.VISIBLE);
                    dialogLayout.findViewById(R.id.deletePost).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            pausePlayers();
                            recentlyDeletedPost = mPostsList.get(getAdapterPosition());
                            recentlyDeletedPostPosition = getAdapterPosition();
                            mPostsList.remove(getAdapterPosition());
                            notifyItemRemoved(getAdapterPosition());
                            dialog.dismiss();
                            showUndoSnackbar();
                        }
                    });
                }
            }

            /**
             * Shows a {@link Snackbar} after a post attempts to be deleted. Contains an action
             * to undo the delete.
             */
            private void showUndoSnackbar() {
                View view = ((Activity)mContext).findViewById(android.R.id.content);
                Snackbar snackbar = Snackbar.make(view, mContext.getResources().getString(R.string.post_deleted_msg), Snackbar.LENGTH_LONG);
                snackbar.show();
                snackbar.setAction(mContext.getResources().getString(R.string.undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        undoDelete();
                    }
                });
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        if(event == BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_SWIPE ||
                            event == BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_TIMEOUT){
                            deletePostDefinitely();
                        }
                    }
                });
            }

            /**
             * After there was no action by the user to undo the delete, the post gets deleted
             * definitely from the db
             */
            private void deletePostDefinitely() {
                final MutableLiveData<Integer> statusCodeLiveData = new MutableLiveData<>();
                if(videoPlayers.get(getAdapterPosition()) != null)
                    videoPlayers.get(getAdapterPosition()).release();
                PostItemRepository repository = new PostItemRepository();
                repository.deletePost(recentlyDeletedPost.getId(),
                        statusCodeLiveData,
                        mPrefs.getString(Constant.PUSHY_TOKEN, ""));
                postsEventsListener.onPostDeleted(statusCodeLiveData);
            }

            /**
             * Inserts the post back into the list and shows the added post in the UI
             */
            private void undoDelete() {
                mPostsList.add(recentlyDeletedPostPosition, recentlyDeletedPost);
                notifyItemChanged(recentlyDeletedPostPosition);
                notifyItemInserted(recentlyDeletedPostPosition);
                postsEventsListener.onUndoPostDeleted(recentlyDeletedPostPosition);
            }
        }
    }
}
