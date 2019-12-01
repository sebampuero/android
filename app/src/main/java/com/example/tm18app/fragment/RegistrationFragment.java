package com.example.tm18app.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.example.tm18app.viewModels.MyViewModel;
import com.example.tm18app.viewModels.RegisterViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegistrationFragment extends Fragment {

    private MultiGoalSelectAdapter adapter;
    private FragmentRegistrationBinding binding;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private MyViewModel mainModel;
    private RegisterViewModel model;

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
        progressBar = binding.progressBarRegistration;
        setupGoalsBoxRecyclerView();
        model.getGoalLiveData().observe(this, new Observer<List<Goal>>() {
            @Override
            public void onChanged(List<Goal> goals) {
                prepareDataForAdapter(goals);
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
        model.getUserLiveData().observe(this, new Observer<HashMap<Integer, User>>() {
            @Override
            public void onChanged(HashMap<Integer, User> integerUserHashMap) {
                evaluateRegistration(integerUserHashMap);
            }
        });
        model.setGoalsAdapter(adapter);
        return binding.getRoot();
    }

    private void evaluateRegistration(HashMap<Integer, User> integerUserHashMap) {
        if(integerUserHashMap.containsKey(500))
            Toast.makeText(this.getContext(),
                    this.getContext().getString(R.string.server_error),
                    Toast.LENGTH_SHORT).show();
        else if(integerUserHashMap.containsKey(400))
            Toast.makeText(this.getContext(),
                    this.getContext().getString(R.string.email_already_exists),
                    Toast.LENGTH_SHORT).show();
        else if(integerUserHashMap.containsKey(200)){
            SharedPreferences introPreferences = this.getActivity().
                    getSharedPreferences(Constant.FIRST_TIME_INTRO,Context.MODE_PRIVATE);
            SharedPreferences.Editor editorIntro = introPreferences.edit();
            editorIntro.putBoolean(Constant.INTRO_OPENED,true);
            editorIntro.commit();
            SharedPreferences sharedPreferences = this.getActivity()
                    .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            User user = integerUserHashMap.get(200);
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
            mainModel.getNavController().navigate(R.id.action_global_feedFragment);
            model.getUserLiveData().getValue().clear();
        }
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
        recyclerView = binding.goalsComboBox;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        adapter = new MultiGoalSelectAdapter();
        recyclerView.setAdapter(adapter);
    }

}
