package com.example.tm18app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.UnicodeSetSpanner;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.FragmentLoginBinding;
import com.example.tm18app.pojos.User;
import com.example.tm18app.viewModels.LoginViewModel;
import com.example.tm18app.viewModels.MyViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;


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
        FragmentLoginBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);
        binding.setMyVM(model);
        binding.setLifecycleOwner(this);
        model.getUserLiveData().observe(this, new Observer<HashMap<Integer, User>>() {
            @Override
            public void onChanged(HashMap<Integer, User> integerUserHashMap) {
                evaluateLogin(integerUserHashMap);
            }
        });
        return binding.getRoot();
    }

    private void evaluateLogin(HashMap<Integer, User> integerUserHashMap) {
        if(integerUserHashMap.containsKey(403))
            Toast.makeText(this.getContext(),
                    this.getContext().getString(R.string.invalid_credentials),
                    Toast.LENGTH_SHORT).show();
        else if(integerUserHashMap.containsKey(500))
            Toast.makeText(this.getContext(),
                    this.getContext().getString(R.string.server_error),
                    Toast.LENGTH_SHORT).show();
        else if(integerUserHashMap.containsKey(200)){
            SharedPreferences introPreferences = this.getActivity().
                    getSharedPreferences(Constant.FIRST_TIME_INTRO,Context.MODE_PRIVATE);
            SharedPreferences.Editor editorIntro = introPreferences.edit();
            editorIntro.putBoolean(Constant.INTRO_OPENED,true);
            editorIntro.commit();
            SharedPreferences sharedPreferences =
                    this.getActivity().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            User user = integerUserHashMap.get(200);
            editor.putBoolean(Constant.LOGGED_IN, true);
            editor.putString(Constant.NAME, user.getName());
            editor.putString(Constant.LASTNAME, user.getLastname());
            editor.putString(Constant.EMAIL, user.getEmail());
            editor.putInt(Constant.USER_ID, user.getId());
            StringBuilder sb = new StringBuilder();
            StringBuilder sb1 = new StringBuilder();
            for (int i = 0; i < user.getGoals().length; i++) {
                sb.append(user.getGoals()[i]).append(",");
                sb1.append(user.getGoalTags()[i]).append(",");
            }
            if(user.getGoalTags().length > 0){
                editor.putString(Constant.GOAL_IDS, sb.toString());
                editor.putString(Constant.GOAL_TAGS, sb1.toString());
            }
            editor.apply();
            mainModel.getNavController().navigate(R.id.action_global_feedFragment);
            model.getUserLiveData().getValue().clear();
        }

    }


}

