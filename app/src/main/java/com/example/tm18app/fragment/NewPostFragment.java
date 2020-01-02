package com.example.tm18app.fragment;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.tm18app.MainActivity;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.R;
import com.example.tm18app.databinding.FragmentNewPostBinding;
import com.example.tm18app.exceptions.FileTooLargeException;
import com.example.tm18app.util.ConverterUtils;
import com.example.tm18app.viewModels.NewPostViewModel;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass. Responsible for UI and events for the new post UI.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class NewPostFragment extends BaseFragmentMediaSelector implements BaseFragmentMediaSelector.BitmapLoaderInterface {

    private NewPostViewModel mModel;
    private FragmentNewPostBinding mBinding;
    private EditText mPostTitleEditText;
    private EditText mPostContentEditText;
    private ImageView mContentIW;
    private PlayerView mContentVW;
    private CircularProgressButton mPostBtn;
    private Button mUploadPicBtn;
    private Button mUploadVideoBtn;
    private SimpleExoPlayer mPlayer;

    public NewPostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setBitmapLoaderInterface(this);
        mModel = ViewModelProviders.of(getActivity()).get(NewPostViewModel.class);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_post, container,
                false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        mModel.setContext(getContext());
        setupViews();
        setSpinner();
        // Trigger loading button for new post
        mModel.triggerLoadingBtn.observe(this, aBoolean -> {
            Toast.makeText(getContext(),
                    getResources().getString(R.string.uploading_post),
                    Toast.LENGTH_LONG).show();
            mPostBtn.startAnimation();
            cleanInputs();
            mMainModel.getNavController().navigateUp();
        });
        return mBinding.getRoot();
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        mPostContentEditText = mBinding.inputTextEdit;
        mPostTitleEditText = mBinding.postTitle;
        mContentIW = mBinding.contentImage;
        mContentVW = mBinding.videpPlayer;
        mPostBtn = mBinding.newPostBtn;
        mUploadPicBtn = mBinding.uploadImageBtn;
        mUploadPicBtn.setOnClickListener(view -> openGalleryForImage());
        mUploadVideoBtn = mBinding.uploadVideoBtn;
        mUploadVideoBtn.setOnClickListener(view -> openGalleryForVideo());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            mContentVW.setVisibility(View.GONE);
            mContentIW.setVisibility(View.VISIBLE);
            Uri contentImgUri = data.getData();
            mModel.setContentImageURI(String.valueOf(contentImgUri));
            processImageURI(contentImgUri, 0, 0);
        }else if(resultCode == RESULT_OK && requestCode == PICK_VIDEO){
            mContentVW.setVisibility(View.VISIBLE);
            mContentIW.setVisibility(View.GONE);
            Uri contentVideoUri = data.getData();
            mModel.setContentVideoURI(String.valueOf(contentVideoUri));
            if(mPlayer != null)
                mPlayer.release();
            TrackSelector selector = new DefaultTrackSelector();
            mPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), selector);
            DefaultDataSourceFactory dataSourceFactory =
                    new DefaultDataSourceFactory(getContext(), "exoplayer_video");
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource source = new ExtractorMediaSource(contentVideoUri,
                    dataSourceFactory,
                    extractorsFactory, null, null);
            mContentVW.setPlayer(mPlayer);
            mPlayer.prepare(source);
            mPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mPlayer != null)
            mPlayer.release();
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap) {
        mContentIW.setImageBitmap(bitmap);
    }

    @Override
    public void onLoadingBitmap() {
        Toast.makeText(getContext(),
                getResources().getString(R.string.loading_image_msg), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onErrorLoadingBitmap() {
        Toast.makeText(getContext(),
                getResources().getString(R.string.error_ocurred), Toast.LENGTH_SHORT).show();
    }

    /**
     * Empties the input fields for the Post
     */
    private void cleanInputs() {
        mPostTitleEditText.setText("");
        mPostContentEditText.setText("");
    }

    /**
     * Sets up the {@link Spinner} that contains the goal tags.
     */
    private void setSpinner() {
        Spinner goalTagsSpinner = mBinding.goalTagsSpinner;
        if(mPrefs.getString(Constant.GOAL_TAGS, null) != null){
            final ArrayList<String> goalTags = new ArrayList<>(Arrays.asList(mPrefs.getString(Constant.GOAL_TAGS, null).split(",")));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, goalTags);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            goalTagsSpinner.setAdapter(adapter);
            goalTagsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    mModel.setSelectedGoalForPost(goalTags.get(i));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    mModel.setSelectedGoalForPost(goalTags.get(0));
                }
            });
        }
    }
}
