package com.example.tm18app.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.R;
import com.example.tm18app.adapters.MultiGoalSelectAdapter;
import com.example.tm18app.databinding.FragmentRegistrationBinding;
import com.example.tm18app.pojos.Goal;
import com.example.tm18app.pojos.GoalItemSelection;
import com.example.tm18app.viewModels.MyViewModel;
import com.example.tm18app.viewModels.RegisterViewModel;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegistrationFragment extends Fragment {

    private MultiGoalSelectAdapter adapter;
    private FragmentRegistrationBinding binding;

    public RegistrationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RegisterViewModel model = ViewModelProviders.of(getActivity()).get(RegisterViewModel.class);
        model.setContext(getContext());
        MyViewModel mainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_registration, container, false);
        binding.setMyVM(model);
        binding.setLifecycleOwner(this);
        model.setNavController(mainModel.getNavController());
        setupGoalsBoxRecyclerView();
        model.getGoalLiveData().observe(this, new Observer<List<Goal>>() {
            @Override
            public void onChanged(List<Goal> goals) {
                prepareDataForAdapter(goals);
            }
        });
        model.setGoalsAdapter(adapter);
        return binding.getRoot();
    }

    private void prepareDataForAdapter(List<Goal> goals) {
        ArrayList<GoalItemSelection> goalItemSelections = new ArrayList<>();
        GoalItemSelection goalItemSelection;
        for(Goal goal : goals){
            goalItemSelection = new GoalItemSelection(goal.getTag(), false, goal.getId());
            goalItemSelections.add(goalItemSelection);
        }
        adapter.setGoals(goalItemSelections);
    }

    private void setupGoalsBoxRecyclerView() {
        RecyclerView recyclerView = binding.goalsComboBox;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        adapter = new MultiGoalSelectAdapter();
        recyclerView.setAdapter(adapter);
    }

}
