package com.example.tm18app.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.R;
import com.example.tm18app.adapters.MultiGoalSelectAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.FragmentRegistrationBinding;
import com.example.tm18app.model.Goal;
import com.example.tm18app.model.GoalItemSelection;
import com.example.tm18app.model.User;
import com.example.tm18app.util.ConverterUtils;
import com.example.tm18app.viewModels.RegisterViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass. Responsible for UI and events for the registration UI.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class RegistrationFragment extends BaseFragmentMediaSelector
        implements BaseFragmentMediaSelector.BitmapLoaderInterface {

    private MultiGoalSelectAdapter mAdapter;
    private FragmentRegistrationBinding mBinding;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private RegisterViewModel mModel;
    private CircularProgressButton mRegistrationBtn;
    private EditText mNameEditText;
    private EditText mLastnameEditText;
    private EditText mPasswordEditText;
    private EditText mPasswordConfEditText;
    private EditText mEmailEditText;
    private ImageView mProfilePicIW;

    public RegistrationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setBitmapLoaderInterface(this);
        mModel = ViewModelProviders.of(this).get(RegisterViewModel.class);
        mModel.setContext(getContext());
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_registration, container, false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        setupViews();
        setupGoalsBoxRecyclerView();
        // Observe to fetch goal items
        mModel.getGoalLiveData().observe(this, goals -> {
            prepareDataForAdapter(goals);
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        });
        // Observe the status of the response of registration process
        mModel.getUserLiveData().observe(this,
                this::evaluateRegistration);
        // Observe the events on the registration button
        mModel.triggerLoadingBtn.observe(this,
                aBoolean -> mRegistrationBtn.startAnimation());
        // Observe for when the open gallery button is clicked
        mModel.selectProfilePic.observe(this,
                aBoolean -> openGalleryForImage());
        mModel.setGoalsAdapter(mAdapter);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            Uri profilePicUri = data.getData();
            processImageURI(profilePicUri, 300, 300);
        }
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap) {
        try {
            mProfilePicIW.setImageBitmap(bitmap);
            byte[] profilePicByteArray = ConverterUtils.getBytes(bitmap);
            mModel.setProfilePicBase64Data(Base64.encodeToString(profilePicByteArray,
                    Base64.DEFAULT));
        }catch (Exception e){
            e.printStackTrace();
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show());
            mProfilePicIW.setVisibility(View.GONE);
        }
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

    @Override
    protected void setupViews() {
        mProgressBar = mBinding.progressBarRegistration;
        mRegistrationBtn = mBinding.registrationBtn;
        mNameEditText = mBinding.nameInput;
        mLastnameEditText = mBinding.lastnameInput;
        mEmailEditText = mBinding.emailAddresInputRegister;
        mPasswordEditText = mBinding.passwordInputRegister;
        mPasswordConfEditText = mBinding.passwordInputRegisterConf;
        mProfilePicIW = mBinding.profilePicRegistration;
    }

    /**
     * Evaluates the registration process and shows feedback to the user
     * @param integerUserHashMap {@link HashMap} map that contains user's info and http
     *                                          response code
     */
    private void evaluateRegistration(HashMap<Integer, User> integerUserHashMap) {
        if(integerUserHashMap.containsKey(HttpURLConnection.HTTP_INTERNAL_ERROR)){
            Toast.makeText(this.getContext(),
                    this.getContext().getString(R.string.server_error),
                    Toast.LENGTH_LONG).show();
            mRegistrationBtn.revertAnimation();
        }
        else if(integerUserHashMap.containsKey(HttpURLConnection.HTTP_BAD_REQUEST)){
            mRegistrationBtn.stopAnimation();
            Toast.makeText(this.getContext(),
                    this.getContext().getString(R.string.email_already_exists),
                    Toast.LENGTH_LONG).show();
            mRegistrationBtn.revertAnimation();
        }
        else if(integerUserHashMap.containsKey(HttpURLConnection.HTTP_OK)){
            User user = integerUserHashMap.get(HttpURLConnection.HTTP_OK);
            handleRegisterSuccess(user);
            View view = getActivity().findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(view,
                    getString(R.string.successfully_registered), Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        mRegistrationBtn.revertAnimation();
    }

    /**
     * Handle the successful registration.
     * @param user {@link User} containing the registered user's info
     */
    private void handleRegisterSuccess(User user) {
        SharedPreferences introPreferences = this.getActivity().
                getSharedPreferences(Constant.FIRST_TIME_INTRO,Context.MODE_PRIVATE);
        SharedPreferences.Editor editorIntro = introPreferences.edit();
        editorIntro.putBoolean(Constant.INTRO_OPENED,true);
        editorIntro.apply();
        mMainModel.getNavController().navigate(R.id.action_registrationFragment_to_loginFragment);
        mModel.getUserLiveData().getValue().clear();
        cleanValues();
    }

    /**
     * Empties input fields
     */
    private void cleanValues() {
        mNameEditText.setText("");
        mLastnameEditText.setText("");
        mEmailEditText.setText("");
        mPasswordConfEditText.setText("");
        mPasswordEditText.setText("");
    }

    /**
     * Prepares the fetched goals data for the mAdapter.
     * @param goals {@link List} containing the fetched goals from the server
     */
    private void prepareDataForAdapter(List<Goal> goals) {
        ArrayList<GoalItemSelection> goalItemSelections = new ArrayList<>();
        GoalItemSelection goalItemSelection;
        for(Goal goal : goals){
            goalItemSelection = new GoalItemSelection(goal.getTag(), false, goal.getId());
            goalItemSelections.add(goalItemSelection);
        }
        mAdapter.setGoals(goalItemSelections);
    }

    /**
     * Sets up the {@link RecyclerView} for the mAdapter
     */
    private void setupGoalsBoxRecyclerView() {
        mRecyclerView = mBinding.goalsComboBox;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                LinearLayoutManager.VERTICAL));
        mAdapter = new MultiGoalSelectAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

}
