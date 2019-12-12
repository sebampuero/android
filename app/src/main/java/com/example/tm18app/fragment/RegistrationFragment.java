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
import com.example.tm18app.pojos.Goal;
import com.example.tm18app.pojos.GoalItemSelection;
import com.example.tm18app.pojos.User;
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
public class RegistrationFragment extends BaseFragmentPictureSelecter implements BaseFragmentPictureSelecter.FromBitmapToUriCallbackInterface {

    private MultiGoalSelectAdapter adapter;
    private FragmentRegistrationBinding binding;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private MyViewModel mainModel;
    private RegisterViewModel model;
    private CircularProgressButton registrationBtn;
    private EditText name;
    private EditText lastname;
    private EditText password;
    private EditText passwordConf;
    private EditText email;
    private ImageView profilePic;
    private Uri imageUri;

    public RegistrationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        model = ViewModelProviders.of(getActivity()).get(RegisterViewModel.class);
        model.setContext(getContext());
        mainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_registration, container, false);
        binding.setMyVM(model);
        binding.setLifecycleOwner(this);
        setIc(this);
        setupViews();
        setupGoalsBoxRecyclerView();
        // Observe to fetch goal items
        model.getGoalLiveData().observe(this, new Observer<List<Goal>>() {
            @Override
            public void onChanged(List<Goal> goals) {
                prepareDataForAdapter(goals);
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
        // Observe the status of the response of registration process
        model.getUserLiveData().observe(this, new Observer<HashMap<Integer, User>>() {
            @Override
            public void onChanged(HashMap<Integer, User> integerUserHashMap) {
                evaluateRegistration(integerUserHashMap);
            }
        });
        // Observe the events on the registration button
        model.triggerLoadingBtn.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                registrationBtn.startAnimation();
            }
        });

        model.selectProfilePic.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                openGallery();
            }
        });
        model.setGoalsAdapter(adapter);
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            imageUri = data.getData();
            applyImageUriToImageView(imageUri, profilePic, 300, 300);
        }
    }

    @Override
    public void uriResultCallback(Uri imageUri) {
        try {
            InputStream iStream = getActivity().getContentResolver().openInputStream(imageUri);
            byte[] profilePicByteArray = ConverterUtils.getBytes(iStream);
            model.setProfilePicBase64Data(Base64.encodeToString(profilePicByteArray, Base64.DEFAULT));
        }catch (Exception e){
            e.printStackTrace();
            profilePic.setVisibility(View.GONE);
        }
    }

    /**
     * Sets up views for this {@link Fragment}
     */
    private void setupViews() {
        progressBar = binding.progressBarRegistration;
        registrationBtn = binding.registrationBtn;
        name = binding.nameInput;
        lastname = binding.lastnameInput;
        email = binding.emailAddresInputRegister;
        password = binding.passwordInputRegister;
        passwordConf = binding.passwordInputRegisterConf;
        profilePic = binding.profilePicRegistration;
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
            registrationBtn.revertAnimation();
        }
        else if(integerUserHashMap.containsKey(HttpURLConnection.HTTP_BAD_REQUEST)){
            registrationBtn.stopAnimation();
            Toast.makeText(this.getContext(),
                    this.getContext().getString(R.string.email_already_exists),
                    Toast.LENGTH_SHORT).show();
            registrationBtn.revertAnimation();
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
        mainModel.getNavController().navigate(R.id.action_registrationFragment_to_feedFragment);
        model.getUserLiveData().getValue().clear();
        registrationBtn.revertAnimation();
        cleanValues();
    }

    /**
     * Empties input fields
     */
    private void cleanValues() {
        name.setText("");
        lastname.setText("");
        email.setText("");
        passwordConf.setText("");
        password.setText("");
    }

    /**
     * Prepares the fetched goals data for the adapter.
     * @param goals {@link List} containing the fetched goals from the server
     */
    private void prepareDataForAdapter(List<Goal> goals) {
        ArrayList<GoalItemSelection> goalItemSelections = new ArrayList<>();
        GoalItemSelection goalItemSelection;
        for(Goal goal : goals){
            goalItemSelection = new GoalItemSelection(goal.getTag(), false, goal.getId());
            goalItemSelections.add(goalItemSelection);
        }
        adapter.setGoals(goalItemSelections);
    }

    /**
     * Sets up the {@link RecyclerView} for the adapter
     */
    private void setupGoalsBoxRecyclerView() {
        recyclerView = binding.goalsComboBox;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        adapter = new MultiGoalSelectAdapter();
        recyclerView.setAdapter(adapter);
    }

}
