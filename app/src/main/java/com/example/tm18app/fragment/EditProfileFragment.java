package com.example.tm18app.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.R;
import com.example.tm18app.adapters.MultiGoalSelectAdapter;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.FragmentEditProfileBinding;
import com.example.tm18app.pojos.Goal;
import com.example.tm18app.pojos.GoalItemSelection;
import com.example.tm18app.pojos.User;
import com.example.tm18app.viewModels.EditViewModel;
import com.example.tm18app.viewModels.MyViewModel;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass. Responsible for UI and events for the profile edition UI.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private MultiGoalSelectAdapter adapter;
    private MyViewModel mainModel;
    private EditViewModel model;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        model = ViewModelProviders.of(getActivity()).get(EditViewModel.class);
        model.setContext(getContext());
        mainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        model.setNavController(mainModel.getNavController());
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false);
        binding.setMyVM(model);
        binding.setLifecycleOwner(this);
        setupGoalsBoxRecyclerView();
        // Observe for clicks on the button that triggers the DialogFragment to request goal tags
        model.navigateToDialog.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                FragmentManager fm = getFragmentManager();
                NewGoalsDialogFragment frag = NewGoalsDialogFragment.newInstance();
                frag.setTargetFragment(EditProfileFragment.this, 0);
                frag.show(fm, "fragment_create_goal"); // start dialog fragment
            }
        });

        // Fetch goal tags with the observer
        model.getGoalLiveData().observe(this, new Observer<List<Goal>>() {
            @Override
            public void onChanged(List<Goal> goals) {
                prepareDataForAdapter(goals);
            }
        });

        // Observe for changes when the user edits his/her info
        model.getUserLiveData().observe(this, new Observer<HashMap<Integer, User>>() {
            @Override
            public void onChanged(HashMap<Integer, User> integerUserHashMap) {
                evaluateEditUser(integerUserHashMap);
            }
        });
        model.setAdapter(adapter);
        return binding.getRoot();
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
            model.getUserLiveData().getValue().clear();
            mainModel.getNavController().navigateUp();
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
        adapter.setGoals(goalItemSelections);
    }

    /**
     * Sets up the {@link RecyclerView} for the Goals
     */
    private void setupGoalsBoxRecyclerView() {
        RecyclerView recyclerView = binding.goalsComboBoxEditProfile;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        adapter = new MultiGoalSelectAdapter();
        recyclerView.setAdapter(adapter);
    }


}
