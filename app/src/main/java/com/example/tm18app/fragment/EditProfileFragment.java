package com.example.tm18app.fragment;


import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.tm18app.R;
import com.example.tm18app.adapters.MultiGoalSelectAdapter;
import com.example.tm18app.databinding.FragmentEditProfileBinding;
import com.example.tm18app.pojos.GoalItemSelection;
import com.example.tm18app.viewModels.EditViewModel;
import com.example.tm18app.viewModels.MyViewModel;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment {

    private EditViewModel model;
    private MyViewModel mainModel;
    private FragmentEditProfileBinding binding;
    private MultiGoalSelectAdapter adapter;
    private RecyclerView recyclerView;
    //TODO: Add progress bar with Mediator

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
        return binding.getRoot();
    }

    private void setupGoalsBoxRecyclerView() {
        recyclerView = binding.goalsComboBoxEditProfile;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        adapter = new MultiGoalSelectAdapter(getActivity());
        recyclerView.setAdapter(adapter);
        model.setGoalsAdapter(adapter);
    }

}
