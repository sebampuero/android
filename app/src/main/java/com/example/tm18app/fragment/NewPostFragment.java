package com.example.tm18app.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.tm18app.MainActivity;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.R;
import com.example.tm18app.databinding.FragmentNewPostBinding;
import com.example.tm18app.util.ConverterUtils;
import com.example.tm18app.viewModels.MyViewModel;
import com.example.tm18app.viewModels.NewPostViewModel;

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
public class NewPostFragment extends BaseFragmentPictureSelecter implements BaseFragmentPictureSelecter.BitmapLoadedInterface{

    private MyViewModel mMainModel;
    private NewPostViewModel mModel;
    private FragmentNewPostBinding mBinding;
    private EditText mPostTitleEditText;
    private EditText mPostContentEditText;
    private ImageView mContentIW;
    private Uri mContentImageURI;
    private CircularProgressButton mPostBtn;

    public NewPostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setBitmapLoaderInterface(this);
        mMainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        mModel = ViewModelProviders.of(getActivity()).get(NewPostViewModel.class);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_post, container, false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        mModel.setContext(getContext());
        setupViews();
        setSpinner();
        // set observer for new post response feedback
        mModel.getPostLiveDataResponse().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer statusCode) {
                evaluatePostResponse(statusCode);
            }
        });
        // Observe for when the open gallery button is clicked
        mModel.selectContentImage.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                openGallery();
            }
        });
        // Trigger loading button for new post
        mModel.triggerLoadingBtn.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                mPostBtn.startAnimation();
            }
        });
        return mBinding.getRoot();
    }

    private void setupViews() {
        mPostContentEditText = mBinding.inputTextEdit;
        mPostTitleEditText = mBinding.postTitle;
        mContentIW = mBinding.contentImage;
        mPostBtn = mBinding.newPostBtn;
        Toolbar toolbar = ((MainActivity)getActivity()).getToolbar();
        toolbar.getMenu().clear();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            mContentImageURI = data.getData();
            applyImageUriToImageView(mContentImageURI, mContentIW, 0, 500);
        }
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap) {
        try {
            // InputStream iStream = getActivity().getContentResolver().openInputStream(mContentImageURI);
            byte[] profilePicByteArray = ConverterUtils.getBytes(bitmap);
            Log.e("TAG", "Length " + profilePicByteArray);
            mModel.setContentImageBase64Data(Base64.encodeToString(profilePicByteArray, Base64.DEFAULT));
        }catch (Exception e){
            e.printStackTrace();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            });
            mContentIW.setVisibility(View.GONE);
        }
    }

    /**
     * Evaluate the status of the procedure of creating a new Post. Show feedback to the user.
     * @param statusCode {@link Integer} HTTP status code of the response from the server
     */
    private void evaluatePostResponse(Integer statusCode) {
        if(statusCode == 200){
            Toast.makeText(this.getContext(), this.getContext().getString(R.string.post_successfully_created), Toast.LENGTH_SHORT).show();
            mMainModel.getNavController().navigateUp();
            mModel.getPostLiveDataResponse().setValue(0);
            cleanInputs();
        }
        else if(statusCode == 500) {
            Toast.makeText(this.getContext(), this.getContext().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
        }
        mPostBtn.revertAnimation();
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
        SharedPreferences preferences = getActivity().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        if(preferences.getString(Constant.GOAL_TAGS, null) != null){
            final ArrayList<String> goalTags = new ArrayList<>(Arrays.asList(preferences.getString(Constant.GOAL_TAGS, null).split(",")));
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
