package com.example.tm18app.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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
import com.example.tm18app.viewModels.MyViewModel;
import com.example.tm18app.viewModels.RegisterViewModel;

import java.io.InputStream;
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
public class RegistrationFragment extends BaseFragmentPictureSelecter{

    private MultiGoalSelectAdapter mAdapter;
    private FragmentRegistrationBinding mBinding;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private MyViewModel mMainModel;
    private RegisterViewModel mModel;
    private CircularProgressButton mRegistrationBtn;
    private EditText mNameEditText;
    private EditText mLastnameEditText;
    private EditText mPasswordEditText;
    private EditText mPasswordConfEditText;
    private EditText mEmailEditText;
    private ImageView mProfilePicIW;
    private Uri mProfilePicURI;

    public RegistrationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mModel = ViewModelProviders.of(getActivity()).get(RegisterViewModel.class);
        mModel.setContext(getContext());
        mMainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_registration, container, false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        setupViews();
        setupGoalsBoxRecyclerView();
        // Observe to fetch goal items
        mModel.getGoalLiveData().observe(this, new Observer<List<Goal>>() {
            @Override
            public void onChanged(List<Goal> goals) {
                prepareDataForAdapter(goals);
                mProgressBar.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        });
        // Observe the status of the response of registration process
        mModel.getUserLiveData().observe(this, new Observer<HashMap<Integer, User>>() {
            @Override
            public void onChanged(HashMap<Integer, User> integerUserHashMap) {
                evaluateRegistration(integerUserHashMap);
            }
        });
        // Observe the events on the registration button
        mModel.triggerLoadingBtn.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                mRegistrationBtn.startAnimation();
            }
        });
        // Observe for when the open gallery button is clicked
        mModel.selectProfilePic.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                openGallery();
            }
        });
        mModel.setGoalsAdapter(mAdapter);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            mProfilePicURI = data.getData();
            applyImageUriToImageView(mProfilePicURI, mProfilePicIW, 300, 300);
            try {
                InputStream iStream = getActivity().getContentResolver().openInputStream(mProfilePicURI);
                byte[] profilePicByteArray = ConverterUtils.getBytes(iStream);
                mModel.setProfilePicBase64Data(Base64.encodeToString(profilePicByteArray, Base64.DEFAULT));
            }catch (Exception e){
                e.printStackTrace();
                mProfilePicIW.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Sets up views for this {@link Fragment}
     */
    private void setupViews() {
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
                    Toast.LENGTH_SHORT).show();
            mRegistrationBtn.revertAnimation();
        }
        else if(integerUserHashMap.containsKey(HttpURLConnection.HTTP_BAD_REQUEST)){
            mRegistrationBtn.stopAnimation();
            Toast.makeText(this.getContext(),
                    this.getContext().getString(R.string.email_already_exists),
                    Toast.LENGTH_SHORT).show();
            mRegistrationBtn.revertAnimation();
        }
        else if(integerUserHashMap.containsKey(HttpURLConnection.HTTP_OK)){
            User user = integerUserHashMap.get(HttpURLConnection.HTTP_OK);
            handleRegisterSuccess(user);
        }
    }

    /**
     * Handle the successful registration. Store relevant user*s info in {@link SharedPreferences}
     * @param user {@link User} containing the registered user's info
     */
    private void handleRegisterSuccess(User user) {
        SharedPreferences introPreferences = this.getActivity().
                getSharedPreferences(Constant.FIRST_TIME_INTRO,Context.MODE_PRIVATE);
        SharedPreferences.Editor editorIntro = introPreferences.edit();
        editorIntro.putBoolean(Constant.INTRO_OPENED,true);
        editorIntro.commit();
        SharedPreferences sharedPreferences = this.getActivity()
                .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constant.LOGGED_IN, true);
        editor.putString(Constant.NAME, user.getName());
        editor.putString(Constant.LASTNAME, user.getLastname());
        editor.putString(Constant.EMAIL, user.getEmail());
        editor.putInt(Constant.USER_ID, user.getId());
        if(user.getProfilePicUrl() != null)
            editor.putString(Constant.PROFILE_PIC_URL, user.getProfilePicUrl());
        if(user.getGoals().length > 0 && user.getGoalTags().length > 0){
            StringBuilder sb = new StringBuilder();
            StringBuilder sb1 = new StringBuilder();
            for (int i = 0; i < user.getGoals().length; i++) {
                sb.append(user.getGoals()[i]).append(",");
                sb1.append(user.getGoalTags()[i]).append(",");
            }
            editor.putString(Constant.GOAL_IDS, sb.toString());
            editor.putString(Constant.GOAL_TAGS, sb1.toString());
        }
        editor.apply();
        mMainModel.getNavController().navigate(R.id.action_registrationFragment_to_feedFragment);
        mModel.getUserLiveData().getValue().clear();
        mRegistrationBtn.revertAnimation();
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
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mAdapter = new MultiGoalSelectAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

}
