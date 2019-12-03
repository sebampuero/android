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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
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
        model.navigateToDialog.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                FragmentManager fm = getFragmentManager();
                NewGoalsDialogFragment frag = NewGoalsDialogFragment.newInstance();
                frag.setTargetFragment(EditProfileFragment.this, 0);
                frag.show(fm, "fragment_create_goal");
            }
        });

        model.getGoalLiveData().observe(this, new Observer<List<Goal>>() {
            @Override
            public void onChanged(List<Goal> goals) {
                prepareDataForAdapter(goals);
            }
        });

        model.getUserLiveData().observe(this, new Observer<HashMap<Integer, User>>() {
            @Override
            public void onChanged(HashMap<Integer, User> integerUserHashMap) {
                evaluateEditUser(integerUserHashMap);
            }
        });
        model.setAdapter(adapter);
        return binding.getRoot();
    }

    private void evaluateEditUser(HashMap<Integer, User> integerUserHashMap) {
        if(integerUserHashMap.containsKey(500)){
            Toast.makeText(this.getContext(), this.getContext().getString(R.string.server_error), Toast.LENGTH_LONG).show();
        }else if(integerUserHashMap.containsKey(400)){
            Toast.makeText(this.getContext(), this.getContext().getString(R.string.email_already_exists), Toast.LENGTH_LONG).show();
        }else if(integerUserHashMap.containsKey(200)){
            SharedPreferences preferences =
                    this.getActivity().getSharedPreferences(Constant.USER_ID, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            User user = integerUserHashMap.get(200);
            editor.clear();
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
            model.getUserLiveData().getValue().clear();
            mainModel.getNavController().navigate(R.id.action_editProfileFragment_to_profileFragment);
        }
    }

    private void prepareDataForAdapter(List<Goal> goals) {
        SharedPreferences preferences = getActivity()
                .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        ArrayList<GoalItemSelection> goalItemSelections = new ArrayList<>();
        GoalItemSelection goalItemSelection;
        for(Goal goal : goals){
            goalItemSelection = new GoalItemSelection(goal.getTag(), false, goal.getId());
            goalItemSelections.add(goalItemSelection);
        }
        if(preferences.getString(Constant.GOAL_TAGS, null) != null){
            List<String> selectedGoals = Arrays.asList(preferences.getString(Constant.GOAL_TAGS, null).split(","));
            for(String goalTag : selectedGoals){
                for(GoalItemSelection item : goalItemSelections){
                    if(goalTag.equals(item.getTag())){
                        item.setChecked(true);
                    }
                }
            }
        }
        adapter.setGoals(goalItemSelections);
    }

    private void setupGoalsBoxRecyclerView() {
        RecyclerView recyclerView = binding.goalsComboBoxEditProfile;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        adapter = new MultiGoalSelectAdapter();
        recyclerView.setAdapter(adapter);
    }


}
