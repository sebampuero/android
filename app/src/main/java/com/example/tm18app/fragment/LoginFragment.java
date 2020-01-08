package com.example.tm18app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import com.example.tm18app.model.User;
import com.example.tm18app.viewModels.LoginViewModel;
import com.example.tm18app.viewModels.MyViewModel;

import java.net.HttpURLConnection;
import java.util.HashMap;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import me.pushy.sdk.Pushy;
import me.pushy.sdk.model.PushyDeviceCredentials;
import me.pushy.sdk.util.exceptions.PushyException;

/**
 * A simple {@link Fragment} subclass. Responsible for UI and events for the login UI.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class LoginFragment extends BaseFragment {

    private LoginViewModel mModel;
    private CircularProgressButton mLoginBtn;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private FragmentLoginBinding mBinding;

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        mModel.setContext(getContext());
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_login, container, false);
        mBinding.setMyVM(mModel);
        mBinding.setLifecycleOwner(this);
        // Observe for response when the user logs in
        mModel.getUserLiveData().observe(this, this::evaluateLogin);
        // Observer to trigger the loading login button animation
        mModel.triggerLoadingBtn.observe(this, aBoolean -> mLoginBtn.startAnimation());
        setupViews();
        return mBinding.getRoot();
    }

    @Override
    protected void setupViews() {
        mLoginBtn = mBinding.loginBtn;
        mEmailEditText = mBinding.emailAddressInput;
        mPasswordEditText = mBinding.passwordInput;
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
        }
        else if(integerUserHashMap.containsKey(HttpURLConnection.HTTP_INTERNAL_ERROR)){ // error from server
            Toast.makeText(this.getContext(),
                    this.getContext().getString(R.string.server_error),
                    Toast.LENGTH_SHORT).show();
        }
        else if(integerUserHashMap.containsKey(HttpURLConnection.HTTP_OK)){
            User user = integerUserHashMap.get(HttpURLConnection.HTTP_OK);
            handleSuccessLogin(user);
        }
        mLoginBtn.revertAnimation();
    }

    /**
     * Sets the user's data locally after a successful login
     * @param user {@link User} the logged in user info
     */
    private void handleSuccessLogin(User user) {
        SharedPreferences introPreferences = this.requireActivity().
                getSharedPreferences(Constant.FIRST_TIME_INTRO,Context.MODE_PRIVATE);
        SharedPreferences.Editor editorIntro = introPreferences.edit();
        editorIntro.putBoolean(Constant.INTRO_OPENED,true);
        editorIntro.apply();
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(Constant.LOGGED_IN, true);
        editor.putString(Constant.NAME, user.getName());
        editor.putString(Constant.LASTNAME, user.getLastname());
        editor.putString(Constant.EMAIL, user.getEmail());
        editor.putInt(Constant.USER_ID, user.getId());
        editor.putString(Constant.PUSHY_TOKEN, user.getPushyToken());
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
        setUserPushyCreds(user);
        mMainModel.getNavController().navigate(R.id.action_loginFragment_to_feedFragment);
        mModel.getUserLiveData().getValue().clear();
        mLoginBtn.revertAnimation();
        cleanInputs();
    }

    /**
     * Registers the device for Pushy to send notifications successfully. The retrieved user from
     * the server contains the pushy token and auth key, those are stored in the internal storage
     * by Pushy.
     * @see PushyDeviceCredentials
     * @param user {@link User}
     */
    private void setUserPushyCreds(User user) {
        final PushyDeviceCredentials credentials =
                new PushyDeviceCredentials(user.getPushyToken(), user.getPushyAuthKey());
        AsyncTask.execute(() -> {
            try {
                Pushy.setDeviceCredentials(credentials, requireContext());
            } catch (PushyException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Empties input fields
     */
    private void cleanInputs() {
        mEmailEditText.setText("");
        mPasswordEditText.setText("");
    }


}

