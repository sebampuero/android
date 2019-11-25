package com.example.tm18app.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import com.example.tm18app.R;
import com.example.tm18app.databinding.FragmentLoginBinding;
import com.example.tm18app.viewModels.LoginViewModel;
import com.example.tm18app.viewModels.MyViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class LoginFragment extends Fragment {

    private LoginViewModel model;
    private MyViewModel mainModel;

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        model = ViewModelProviders.of(getActivity()).get(LoginViewModel.class);
        model.setContext(getContext());
        mainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        //mainModel.getActionBar().hide();
        FragmentLoginBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);
        binding.setMyVM(model);
        binding.setLifecycleOwner(this);
        model.setNavController(mainModel.getNavController());
        return binding.getRoot();
    }


}

