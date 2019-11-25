package com.example.tm18app.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.R;
import com.example.tm18app.databinding.FragmentNewPostBinding;
import com.example.tm18app.viewModels.MyViewModel;
import com.example.tm18app.viewModels.NewPostViewModel;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewPostFragment extends Fragment {

    private MyViewModel mainModel;
    private NewPostViewModel model;
    private FragmentNewPostBinding binding;

    public NewPostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        model = ViewModelProviders.of(getActivity()).get(NewPostViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_post, container, false);
        binding.setMyVM(model);
        binding.setLifecycleOwner(this);
        model.setNavController(mainModel.getNavController());
        model.setContext(getActivity());
        setSpinner(binding.goalTagsSpinner);
        return binding.getRoot();
    }

    private void setSpinner(Spinner goalTagsSpinner) {
        SharedPreferences preferences = getActivity().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        final ArrayList<String> goalTags = new ArrayList<>(Arrays.asList(preferences.getString(Constant.GOAL_TAGS, null).split(",")));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, goalTags);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        goalTagsSpinner.setAdapter(adapter);
        goalTagsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                model.setSelectedGoalForPost(goalTags.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                model.setSelectedGoalForPost(goalTags.get(0));
            }
        });
    }

}
