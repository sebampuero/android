package com.example.tm18app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.FragmentLoginBinding;
import com.example.tm18app.pojos.User;
import com.example.tm18app.viewModels.LoginViewModel;
import com.example.tm18app.viewModels.MyViewModel;

import java.net.HttpURLConnection;
import java.util.HashMap;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

/**
 * A simple {@link Fragment} subclass. Responsible for UI and events for the login UI.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class LoginFragment extends Fragment {

    private LoginViewModel model;
    private MyViewModel mainModel;
    private CircularProgressButton loginBtn;
    private EditText email;
    private EditText password;

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
        loginBtn = binding.loginBtn;
        email = binding.emailAddressInput;
        password = binding.passwordInput;
        // Observe for response when the user logs in
        model.getUserLiveData().observe(this, new Observer<HashMap<Integer, User>>() {
            @Override
            public void onChanged(HashMap<Integer, User> integerUserHashMap) {
                evaluateLogin(integerUserHashMap);
            }
        });
        // Observer to trigger the loading login button animation
        model.triggerLoadingBtn.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                loginBtn.startAnimation();
            }
        });
        return binding.getRoot();
    }

    /**
     * Evaluates the log in process. Shows feedback to the user accordingly.
     * @param integerUserHashMap {@link HashMap} containing the HTTP Status code of the operation
     *                                         and the user's information
     */
    private void evaluateLogin(HashMap<Integer, User> integerUserHashMap) {
        if(integerUserHashMap.containsKey(HttpURLConnection.HTTP_FORBIDDEN)){ // not authenticated, invalid credentials
            Toast.makeText(this.getContext(),
                    this.getContext().getString(R.string.invalid_credentials),
                    Toast.LENGTH_SHORT).show();
            loginBtn.revertAnimation();
        }
        else if(integerUserHashMap.containsKey(HttpURLConnection.HTTP_INTERNAL_ERROR)){ // error from server
            Toast.makeText(this.getContext(),
                    this.getContext().getString(R.string.server_error),
                    Toast.LENGTH_SHORT).show();
            loginBtn.revertAnimation();
        }
        else if(integerUserHashMap.containsKey(HttpURLConnection.HTTP_OK)){
            User user = integerUserHashMap.get(HttpURLConnection.HTTP_OK);
            handleSuccessLogin(user);
        }

    }

    /**
     * Sets the user's data locally after a successful login
     * @param user {@link User} the logged in user info
     */
    private void handleSuccessLogin(User user) {
        SharedPreferences introPreferences = this.getActivity().
                getSharedPreferences(Constant.FIRST_TIME_INTRO,Context.MODE_PRIVATE);
        SharedPreferences.Editor editorIntro = introPreferences.edit();
        editorIntro.putBoolean(Constant.INTRO_OPENED,true);
        editorIntro.commit();
        SharedPreferences sharedPreferences =
                this.getActivity().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constant.LOGGED_IN, true);
        editor.putString(Constant.NAME, user.getName());
        editor.putString(Constant.LASTNAME, user.getLastname());
        editor.putString(Constant.EMAIL, user.getEmail());
        editor.putInt(Constant.USER_ID, user.getId());
        if(user.getProfilePicUrl() != null)
            editor.putString(Constant.PROFILE_PIC_URL, user.getProfilePicUrl());
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
        mainModel.getNavController().navigate(R.id.action_loginFragment_to_feedFragment);
        model.getUserLiveData().getValue().clear();
        loginBtn.revertAnimation();
        cleanInputs();
    }

    /**
     * Empties input fields
     */
    private void cleanInputs() {
        email.setText("");
        password.setText("");
    }


}

