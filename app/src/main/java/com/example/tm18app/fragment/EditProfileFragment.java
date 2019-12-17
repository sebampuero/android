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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;
import com.example.tm18app.adapters.MultiGoalSelectAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.FragmentEditProfileBinding;
import com.example.tm18app.model.Goal;
import com.example.tm18app.model.GoalItemSelection;
import com.example.tm18app.model.User;
import com.example.tm18app.util.ConverterUtils;
import com.example.tm18app.viewModels.EditViewModel;
import com.example.tm18app.viewModels.MyViewModel;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass. Responsible for UI and events for the profile edition UI.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class EditProfileFragment extends BaseFragmentPictureSelecter{

    private FragmentEditProfileBinding mBinding;
    private MultiGoalSelectAdapter mAdapter;
    private MyViewModel mMainModel;
    private EditViewModel mModel;
    private Uri mProfilePicURI;
    private ImageView mProfilePicIW;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mModel = ViewModelProviders.of(getActivity()).get(EditViewModel.class);
        mModel.setContext(getContext());
        mMainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        mModel.setNavController(mMainModel.getNavController());
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        setupViews();
        setupGoalsBoxRecyclerView();
        // Observe for clicks on the button that triggers the DialogFragment to request goal tags
        mModel.navigateToDialog.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                FragmentManager fm = getFragmentManager();
                NewGoalsDialogFragment frag = NewGoalsDialogFragment.newInstance();
                frag.setTargetFragment(EditProfileFragment.this, 0);
                frag.show(fm, "fragment_create_goal"); // start dialog fragment
            }
        });

        // Fetch goal tags with the observer
        mModel.getGoalLiveData().observe(this, new Observer<List<Goal>>() {
            @Override
            public void onChanged(List<Goal> goals) {
                prepareDataForAdapter(goals);
            }
        });

        // Observe for changes when the user edits his/her info
        mModel.getUserLiveData().observe(this, new Observer<HashMap<Integer, User>>() {
            @Override
            public void onChanged(HashMap<Integer, User> integerUserHashMap) {
                evaluateEditUser(integerUserHashMap);
            }
        });
        // Observe for when the button to open gallery is clicked
        mModel.selectProfilePic.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                openGallery();
            }
        });
        mModel.setAdapter(mAdapter);
        fetchProfilePic();
        return mBinding.getRoot();
    }

    private void setupViews() {
        mProfilePicIW = mBinding.profilePic;
        Toolbar toolbar = ((MainActivity)getActivity()).getToolbar();
        toolbar.getMenu().clear();
    }

    /**
     * Fetches the profile pic with {@link Picasso} and sets it into the {@link ImageView} profile
     * pic
     */
    private void fetchProfilePic() {
        SharedPreferences prefs =
                getContext().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        String imgUrl = prefs.getString(Constant.PROFILE_PIC_URL, null);
        if(imgUrl != null){
            if(!imgUrl.equals("")){
                Picasso.get().load(prefs.getString(Constant.PROFILE_PIC_URL, null))
                        .resize(300, 300).centerCrop().into(mProfilePicIW);
            }
        }
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
     * Evaluates the info that came from the database and show corresponding feedback messages
     * to the user about the edition process.
     * @param integerUserHashMap {@link HashMap} containing the HTTP Status code of the operation
     *                                          and the user's information
     */
    private void evaluateEditUser(HashMap<Integer, User> integerUserHashMap) {
        if(integerUserHashMap.containsKey(HttpURLConnection.HTTP_INTERNAL_ERROR)){ // error from server
            Toast.makeText(this.getContext(), this.getContext().getString(R.string.server_error), Toast.LENGTH_LONG).show();
        }else if(integerUserHashMap.containsKey(HttpURLConnection.HTTP_BAD_REQUEST)){ // email address exists already, bad request
            Toast.makeText(this.getContext(), this.getContext().getString(R.string.email_already_exists), Toast.LENGTH_LONG).show();
        }else if(integerUserHashMap.containsKey(HttpURLConnection.HTTP_OK)){ // everything ok
            SharedPreferences preferences =
                    this.getActivity().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            User user = integerUserHashMap.get(HttpURLConnection.HTTP_OK);
            editor.clear();
            // Update local user info
            editor.putBoolean(Constant.LOGGED_IN, true);
            editor.putString(Constant.NAME, user.getName());
            editor.putString(Constant.LASTNAME, user.getLastname());
            editor.putString(Constant.EMAIL, user.getEmail());
            editor.putInt(Constant.USER_ID, user.getId());
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
            Toast.makeText(this.getContext(), this.getContext().getString(R.string.profile_edit_success_msg), Toast.LENGTH_SHORT).show();
            // reset HashMap otherwise the fragment keeps thinking there are changes every time it
            // is opened
            mModel.getUserLiveData().getValue().clear();
            mMainModel.getNavController().navigateUp();
        }
    }

    /**
     * Process the fetched goals list
     * @param goals {@link List} of {@link Goal} items
     */
    private void prepareDataForAdapter(List<Goal> goals) {
        SharedPreferences preferences = getActivity()
                .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        // Populate a list of GoalItemSelection items for the Adapter to handle the goals displaying
        // correctly
        ArrayList<GoalItemSelection> goalItemSelections = new ArrayList<>();
        GoalItemSelection goalItemSelection;
        for(Goal goal : goals){
            goalItemSelection = new GoalItemSelection(goal.getTag(), false, goal.getId());
            goalItemSelections.add(goalItemSelection);
        }
        if(preferences.getString(Constant.GOAL_TAGS, null) != null){ // if user has checked goals
            List<String> selectedGoals =
                    Arrays.asList(preferences.getString(Constant.GOAL_TAGS, null).split(","));
            for(String goalTag : selectedGoals){
                for(GoalItemSelection item : goalItemSelections){
                    if(goalTag.equals(item.getTag())){
                        item.setChecked(true); // display checked user's goals in the recycler view
                    }
                }
            }
        }
        mAdapter.setGoals(goalItemSelections);
    }

    /**
     * Sets up the {@link RecyclerView} for the Goals
     */
    private void setupGoalsBoxRecyclerView() {
        RecyclerView recyclerView = mBinding.goalsComboBoxEditProfile;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mAdapter = new MultiGoalSelectAdapter();
        recyclerView.setAdapter(mAdapter);
    }

}
