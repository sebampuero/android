package com.example.tm18app.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.R;
import com.example.tm18app.adapters.MultiGoalSelectAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.FragmentEditProfileBinding;
import com.example.tm18app.model.Goal;
import com.example.tm18app.model.GoalItemSelection;
import com.example.tm18app.model.User;
import com.example.tm18app.util.ConverterUtils;
import com.example.tm18app.viewModels.EditViewModel;
import com.squareup.picasso.Picasso;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass. Responsible for UI and events for the profile edition UI.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class EditProfileFragment extends BaseFragmentMediaSelector
        implements BaseFragmentMediaSelector.BitmapLoaderInterface {

    private FragmentEditProfileBinding mBinding;
    private MultiGoalSelectAdapter mAdapter;
    private EditViewModel mModel;
    private ImageView mProfilePicIW;
    private CircularProgressButton mSaveBtn;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setBitmapLoaderInterface(this);
        mModel = ViewModelProviders.of(this).get(EditViewModel.class);
        mModel.setContext(getContext());
        mModel.setNavController(mMainModel.getNavController());
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        setupViews();
        setupGoalsBoxRecyclerView();
        // Observe for clicks on the button that triggers the DialogFragment to request goal tags
        mModel.mNavigateToDialog.observe(this, aBoolean -> {
            FragmentManager fm = getFragmentManager();
            NewGoalsDialogFragment frag = NewGoalsDialogFragment.newInstance();
            frag.setTargetFragment(EditProfileFragment.this, 0);
            frag.show(fm, "fragment_create_goal"); // start dialog fragment
        });

        // Fetch goal tags with the observer
        mModel.getGoalLiveData().observe(this, this::prepareDataForAdapter);

        // Observe for changes when the user edits his/her info
        mModel.getUserLiveData().observe(this, this::evaluateEditUser);
        // Observe for when the button to open gallery is clicked
        mModel.mSelectProfilePic.observe(this, aBoolean -> openGalleryForImage());
        mModel.mTriggerLoadingBtn.observe(this, aBoolean -> mSaveBtn.startAnimation());
        mModel.setAdapter(mAdapter);
        fetchProfilePic();
        return mBinding.getRoot();
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        mSaveBtn = mBinding.saveEditProfileBtn;
        mProfilePicIW = mBinding.profilePic;
    }

    /**
     * Fetches the profile pic with {@link Picasso} and sets it into the {@link ImageView} profile
     * pic
     */
    private void fetchProfilePic() {
        SharedPreferences prefs =
                requireContext().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        String imgUrl = prefs.getString(Constant.PROFILE_PIC_URL, null);
        if(imgUrl != null){
            if(!imgUrl.equals("")){
                Picasso.get().load(prefs.getString(Constant.PROFILE_PIC_URL, null))
                        .into(mProfilePicIW);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            Uri profilePicUri = data.getData();
            processImageURI(profilePicUri, 300,300);
        }
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap) {
        try {
            mProfilePicIW.setImageBitmap(bitmap);
            byte[] profilePicByteArray = ConverterUtils.getBytes(bitmap);
            mModel.setProfilePicBase64Data(Base64.encodeToString(profilePicByteArray, Base64.DEFAULT));
        }catch (Exception e){
            e.printStackTrace();
            requireActivity().runOnUiThread(() ->
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


    /**
     * Evaluates the info that came from the database and show corresponding feedback messages
     * to the user about the edition process.
     * @param integerUserHashMap {@link HashMap} containing the HTTP Status code of the operation
     *                                          and the user's information
     */
    private void evaluateEditUser(HashMap<Integer, User> integerUserHashMap) {
        if(integerUserHashMap.containsKey(HttpURLConnection.HTTP_INTERNAL_ERROR)){ // error from server
            Toast.makeText(this.getContext(), this.getContext().getString(R.string.server_error), Toast.LENGTH_LONG).show();
        }else if(integerUserHashMap.containsKey(HttpURLConnection.HTTP_BAD_REQUEST)){ // mEmail address exists already, bad request
            Toast.makeText(this.getContext(), this.getContext().getString(R.string.email_already_exists), Toast.LENGTH_LONG).show();
        }else if(integerUserHashMap.containsKey(HttpURLConnection.HTTP_OK)){ // everything ok
            SharedPreferences preferences =
                    this.requireActivity().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
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
            editor.putString(Constant.PUSHY_TOKEN, user.getPushyToken());
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
        mSaveBtn.revertAnimation();
    }

    /**
     * Process the fetched goals list
     * @param goals {@link List} of {@link Goal} items
     */
    private void prepareDataForAdapter(List<Goal> goals) {
        SharedPreferences preferences = requireActivity()
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
            String[] selectedGoals =
                    preferences.getString(Constant.GOAL_TAGS, "").split(",");
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
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(),
                LinearLayoutManager.VERTICAL));
        mAdapter = new MultiGoalSelectAdapter();
        recyclerView.setAdapter(mAdapter);
    }
}
