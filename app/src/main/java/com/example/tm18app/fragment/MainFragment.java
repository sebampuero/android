package com.example.tm18app.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.tm18app.R;
import com.example.tm18app.databinding.FragmentMainBinding;
import com.example.tm18app.viewModels.MyViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {
    private MyViewModel model;
    private FragmentMainBinding binding;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        model = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        binding.setMyVM(model);
        binding.setLifecycleOwner(this);
        //model.getActionBar().hide();
        return binding.getRoot();
    }


}
