package com.example.tm18app.fragment;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.tm18app.R;
import com.example.tm18app.adapters.MultiGoalSelectAdapter;
import com.example.tm18app.databinding.FragmentRegistrationBinding;
import com.example.tm18app.network.FetchGoalsAsyncTask;
import com.example.tm18app.network.GoalsRestInterface;
import com.example.tm18app.network.RetrofitNetworkConnectionSingleton;
import com.example.tm18app.network.UserRestInterface;
import com.example.tm18app.pojos.Goal;
import com.example.tm18app.pojos.GoalItemSelection;
import com.example.tm18app.viewModels.MyViewModel;
import com.example.tm18app.viewModels.RegisterViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicIntegerArray;

import retrofit2.Call;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegistrationFragment extends Fragment {
    private MyViewModel mainModel;
    private RegisterViewModel model;

    private ArrayList<GoalItemSelection> goals = new ArrayList<>();
    private MultiGoalSelectAdapter adapter;
    private RecyclerView recyclerView;
    private FragmentRegistrationBinding binding;

    public RegistrationFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        model = ViewModelProviders.of(getActivity()).get(RegisterViewModel.class);
        model.setContext(getContext());
        mainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        //mainModel.getActionBar().hide();
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_registration, container, false);
        binding.setMyVM(model);
        binding.setLifecycleOwner(this);
        model.setNavController(mainModel.getNavController());
        setupGoalsBoxRecyclerView();
        return binding.getRoot();
    }

    private void setupGoalsBoxRecyclerView() {
        recyclerView = binding.goalsComboBox;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        adapter = new MultiGoalSelectAdapter(getActivity(), goals);
        recyclerView.setAdapter(adapter);
        model.setGoalsAdapter(adapter);
        //TODO: move fetchGoals to RegisterViewModel, because ViewModels survives rotations and does not need to call the API again
        fetchGoals();
    }


    private void fetchGoals() {
        new FetchGoalsAsyncTask(adapter, getContext()).execute();
    }

}
