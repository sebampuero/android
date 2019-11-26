package com.example.tm18app.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.example.tm18app.viewModels.EditViewModel;
import com.example.tm18app.viewModels.MyViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private MultiGoalSelectAdapter adapter;
    //TODO: Add progress bar with Mediator

    public EditProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EditViewModel model = ViewModelProviders.of(getActivity()).get(EditViewModel.class);
        model.setContext(getContext());
        MyViewModel mainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
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
        model.setAdapter(adapter);
        return binding.getRoot();
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
