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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.PostCardviewBinding;
import com.example.tm18app.fragment.CommentSectionFragment;
import com.example.tm18app.fragment.OtherProfileFragment;
import com.example.tm18app.fragment.PostImgFragment;
import com.example.tm18app.network.CacheDataSourceFactory;
import com.example.tm18app.network.NetworkConnectivity;
import com.example.tm18app.model.Post;
import com.example.tm18app.repository.PostItemRepository;
import com.example.tm18app.util.ConverterUtils;
import com.example.tm18app.util.DialogManager;
import com.example.tm18app.util.TimeUtils;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.video.VideoListener;
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
public class PostItemAdapter extends RecyclerView.Adapter<PostItemAdapter.ItemViewHolder> {

    private ArrayList<Post> mPostsList;
    private NavController mNavController;
    private Context mContext;
    private SharedPreferences mPrefs;
    private int mProfilePicDimen;
    private HashMap<Integer, SimpleExoPlayer> mVideoPlayers;
    private Post mRecentlyDeletedPost;
    private int mRecentlyDeletedPostPosition;
    private PostsEventsListener mPostsEventListener;

    private final String TAG = getClass().getSimpleName();

    /**
     * Listener for posts events
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

        /**
         * Called when a video is clicked to be played fullscreen on landscape mode
         * @param videoUrl {@link String} URL of the video
         * @param seekPoint {@link Long} current position in milliseconds of the video
         */
        default void onFullscreen(String videoUrl, long seekPoint) {}

    }

    public PostItemAdapter(ArrayList<Post> posts, NavController mNavController,
                           Context context, PostsEventsListener postsEventsListener) {
        this.mPostsList = posts;
        this.mNavController = mNavController;
        this.mContext = context;
        mPrefs = context.getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        mProfilePicDimen = context.getResources().getInteger(R.integer.thumbnail_profile_pic);
        mVideoPlayers = new HashMap<>();
        this.mPostsEventListener = postsEventsListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        PostCardviewBinding itemBinding = PostCardviewBinding.inflate(layoutInflater, parent, false);
        return new ItemViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
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
        if(mVideoPlayers != null)
            if(!mVideoPlayers.isEmpty()){
                for(SimpleExoPlayer player : mVideoPlayers.values()){
                    player.release();
                }
            }
    }

    /**
     * Pauses all instantiated {@link com.google.android.exoplayer2.ExoPlayer} inside the {@link HashMap}
     */
    public void pausePlayers() {
        if(mVideoPlayers != null)
            if(!mVideoPlayers.isEmpty()){
                for(SimpleExoPlayer player : mVideoPlayers.values()){
                    player.setPlayWhenReady(false);
                }
            }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder{

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
        ImageView playBtnView;
        ProgressBar progressBarVideo;
        PlayerView surfaceView;
        RelativeLayout postMediaContent;
        ImageView playPauseBtn;
        ImageView fullScreenBtn;

        ItemViewHolder(final PostCardviewBinding binding) {
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
            postMediaContent = binding.postMediaContent;
            playBtnView = binding.playBtnView;
            playPauseBtn = binding.playPauseBtn;
            fullScreenBtn = binding.fullScreenBtn;
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
            if(post.getTitle() != null)
                postTitle.setText(post.getTitle());
            else
                postTitle.setVisibility(View.GONE);
            postContent.setText(post.getContent());
            goalTag.setText(post.getGoalTag());
            commentCount.setText(String.valueOf(post.getCommentCount()));
            timestamp.setText(TimeUtils.parseTimestampToLocaleDatetime(post.getTimestamp(), mContext));
            commentsSection.setOnClickListener(new CommentsClickListener());
            posterProfilePic.setOnClickListener(new ProfilePicClickListener());
            moreVertOptions.setOnClickListener(new OptionsClickListener(subscriberIds));
            if(post.getPosterPicUrl() != null)
                Picasso.get()
                        .load(post.getPosterPicUrl()) // no need to tweak quality
                        .resize(mProfilePicDimen, mProfilePicDimen)
                        .centerCrop()
                        .placeholder(R.drawable.ic_person_black_24dp)
                        .into(posterProfilePic);
            else
                posterProfilePic.setImageDrawable(mContext.getDrawable(R.drawable.ic_person_black_24dp));
            if(post.getContentPicUrl() != null){
                postMediaContent.setVisibility(View.VISIBLE);
                String imgUrl =  NetworkConnectivity // download on low quality if on metered connection
                        .tweakImgQualityByNetworkType(mContext,
                                post.getContentPicUrl());
                String pictureCacheKey = ConverterUtils.extractUrlKey(imgUrl);
                Picasso.get()
                        .load(imgUrl)
                        .placeholder(R.drawable.placeholder)
                        .stableKey(pictureCacheKey)
                        .into(contentImage);
                contentImage.setOnClickListener(new ImageClickListener());
            }
            if(post.getContentVideoUrl() != null){
                postMediaContent.setVisibility(View.VISIBLE);
                String thumbnailUrl = NetworkConnectivity.tweakImgQualityByNetworkType(mContext,
                        post.getContentVideoThumbnailUrl());
                String thumbnailCacheKey = ConverterUtils.extractUrlKey(thumbnailUrl);
                playBtnView.setVisibility(View.VISIBLE); // show that this post is video
                fullScreenBtn.setVisibility(View.GONE); // hide full screen btn if another one was instantiated for post
                surfaceView.setVisibility(View.GONE); // hide player if another one was instantiated for this position
                playPauseBtn.setVisibility(View.GONE); // hide play/pause btn if another one was instantiated for this position
                contentImage.setVisibility(View.VISIBLE); // show thumbnail of video
                Picasso.get()
                        .load(thumbnailUrl)
                        .stableKey(thumbnailCacheKey)
                        .into(contentImage);
                contentImage.setOnClickListener(new VideoThumbnailClickListener());
            }
            if(post.getContentVideoUrl() == null && post.getContentPicUrl() == null)
                postMediaContent.setVisibility(View.GONE);
        }

        /**
         * Comments click mPostsEventListener.
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
         * Profile picture click mPostsEventListener.
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
         * Content image click mPostsEventListener.
         */
        class ImageClickListener implements View.OnClickListener {

            @Override
            public void onClick(View view) {
                Post post = mPostsList.get(getAdapterPosition());
                Bundle bundle = new Bundle();
                bundle.putString(PostImgFragment.IMG_URL,
                        post.getContentPicUrl());
                mNavController.navigate(R.id.postImgFragment, bundle);

            }
        }

        /**
         * Videothumbnail click mPostsEventListener. Upon click on the video thumbnail the video
         * should start buffering and playing.
         * @see PlayerListener
         */
        class VideoThumbnailClickListener implements View.OnClickListener {

            @Override
            public void onClick(View view) {
                playPauseBtn.setVisibility(View.VISIBLE);
                playPauseBtn.setOnClickListener(view1 -> {
                    SimpleExoPlayer player = mVideoPlayers.get(getAdapterPosition());
                    // play or pause the video upon click on the play/pause btn
                    player.setPlayWhenReady(!player.getPlayWhenReady());
                });
                fullScreenBtn.setVisibility(View.VISIBLE);
                fullScreenBtn.setOnClickListener(view12 -> {
                    Player player = mVideoPlayers.get(getAdapterPosition());
                    if(player != null){
                        mPostsEventListener.onFullscreen(mPostsList.get(getAdapterPosition())
                                .getContentVideoUrl(), player.getContentPosition());
                    }
                });
                surfaceView.setVisibility(View.VISIBLE);
                playBtnView.setVisibility(View.GONE);
                //Init video player params
                BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                TrackSelection.Factory videoTrackSelectionFactory =
                        new AdaptiveTrackSelection.Factory(bandwidthMeter);
                TrackSelector trackSelector =
                        new DefaultTrackSelector(videoTrackSelectionFactory);
                SimpleExoPlayer videoPlayer = ExoPlayerFactory
                        .newSimpleInstance(mContext, trackSelector);
                // pause other players is another player is currently playing video
                pausePlayers();
                // if there is already a videoplayer instance in the position index, release it to
                // not occupy too much memory
                if(mVideoPlayers.get(getAdapterPosition()) != null)
                    mVideoPlayers.get(getAdapterPosition()).release();
                mVideoPlayers.put(getAdapterPosition(), videoPlayer);
                surfaceView.setUseController(false);
                surfaceView.setPlayer(videoPlayer);
                // occupy whole width of screen
                surfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                videoPlayer.addListener(new PlayerListener());

                String contentUrl = mPostsList.get(getAdapterPosition()).getContentVideoUrl();
                MediaSource videoSource = new ExtractorMediaSource(Uri.parse(contentUrl),
                        new CacheDataSourceFactory(mContext, // init cache params
                                20 * 1024 * 1024, // 20mb
                                50 * 1024 * 1024), // 50mb
                        new DefaultExtractorsFactory(), null, null);
                videoPlayer.prepare(videoSource);
                videoPlayer.setPlayWhenReady(true);
                videoPlayer.addVideoListener(new VideoListenerImpl());
            }

        }

        /**
         * {@link VideoListener} implementation.
         */
        class VideoListenerImpl implements VideoListener {

            @Override
            public void onVideoSizeChanged(int width, int height,
                                           int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                // Listen for changes on the video size of the buffered video and set a height to the
                // player view dynamically, this because playerview has no maxHeight attr
                int minVideoHeight = mContext.getResources().getInteger(R.integer.min_video_height);
                int maxVideoHeight = mContext.getResources().getInteger(R.integer.max_video_height);
                RelativeLayout.LayoutParams params =
                        new RelativeLayout.LayoutParams(surfaceView.getWidth(), surfaceView.getHeight());
                int heightInDp = ConverterUtils.dpToPx(height, mContext);
                if(heightInDp > maxVideoHeight){
                    heightInDp = maxVideoHeight;
                    params.height = heightInDp;
                    surfaceView.setLayoutParams(params);
                }
                else if(heightInDp < minVideoHeight){
                    heightInDp = minVideoHeight;
                    params.height = heightInDp;
                    surfaceView.setLayoutParams(params);
                }
            }
        }

        /**
         * Videoplayer events mPostsEventListener.
         */
        class PlayerListener implements Player.EventListener {

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if(isPlaying)
                    playPauseBtn.setImageDrawable(mContext.getDrawable(R.drawable.ic_pause_white_24dp));
                else
                    playPauseBtn.setImageDrawable(mContext.getDrawable(R.drawable.ic_play_arrow_white_24dp));
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case Player.STATE_ENDED: // at end of video, play again
                        Player player = mVideoPlayers.get(getAdapterPosition());
                        if(player != null)
                            player.seekTo(0);
                    case Player.STATE_BUFFERING:
                        progressBarVideo.setVisibility(View.VISIBLE);
                        break;
                    case Player.STATE_READY:
                        progressBarVideo.setVisibility(View.GONE);
                        surfaceView.setVisibility(View.VISIBLE);
                        contentImage.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        }

        /**
         * Post options click mPostsEventListener
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
                DialogManager
                        .getInstance()
                        .showCustomDialog(
                                R.layout.post_options_alert,
                                mContext,
                                (dialogCustomView, dialog) -> {
                                    dialogCustomView.findViewById(R.id.reportPost).setOnClickListener(view13 -> {
                                        Toast.makeText(mContext,
                                                "Not yet implemented!", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    });
                                    if(subscriberIds != null){
                                        // if current user is subscribed to this post
                                        if(subscriberIds.indexOf(String.valueOf(userID)) >= 0) {
                                            dialogCustomView.findViewById(R.id.unsubscribe).setVisibility(View.VISIBLE);
                                            dialogCustomView.findViewById(R.id.unsubscribe).setOnClickListener(view12 -> {
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
                                            });
                                        }
                                    }
                                    if(post.getUserID() == userID){
                                        dialogCustomView.findViewById(R.id.deletePost).setVisibility(View.VISIBLE);
                                        dialogCustomView.findViewById(R.id.deletePost).setOnClickListener(view1 -> {
                                            pausePlayers();
                                            mRecentlyDeletedPost = mPostsList.get(getAdapterPosition());
                                            mRecentlyDeletedPostPosition = getAdapterPosition();
                                            mPostsList.remove(getAdapterPosition());
                                            notifyItemRemoved(getAdapterPosition());
                                            dialog.dismiss();
                                            showUndoSnackbar();
                                        });
                                    }
                                }
                        );
            }

            /**
             * Shows a {@link Snackbar} after a post attempts to be deleted. Contains an action
             * to undo the delete.
             */
            private void showUndoSnackbar() {
                View view = ((Activity)mContext).findViewById(android.R.id.content);
                Snackbar snackbar = Snackbar.make(view, mContext.getResources().getString(R.string.post_deleted_msg), 1500);
                snackbar.show();
                snackbar.setAction(mContext.getResources().getString(R.string.undo), view1 -> undoDelete());
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
                if(mVideoPlayers.get(getAdapterPosition()) != null)
                    mVideoPlayers.get(getAdapterPosition()).release();
                PostItemRepository repository = new PostItemRepository();
                repository.deletePost(mRecentlyDeletedPost.getId(),
                        statusCodeLiveData,
                        mPrefs.getString(Constant.PUSHY_TOKEN, ""));
                mPostsEventListener.onPostDeleted(statusCodeLiveData);
            }

            /**
             * Inserts the post back into the list and shows the added post in the UI
             */
            private void undoDelete() {
                mPostsList.add(mRecentlyDeletedPostPosition, mRecentlyDeletedPost);
                notifyItemChanged(mRecentlyDeletedPostPosition);
                notifyItemInserted(mRecentlyDeletedPostPosition);
                mPostsEventListener.onUndoPostDeleted(mRecentlyDeletedPostPosition);
            }
        }
    }
}
