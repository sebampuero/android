package com.example.tm18app.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.network.PasswordResetAsyncTask;
import com.example.tm18app.pojos.PasswordReset;

/**
 * A simple {@link Fragment} subclass. Responsible for UI and events for the password edition UI.
 * No need to attach a View Model for this {@link Fragment}
 */
public class EditPasswordFragment extends Fragment {

    private TextView oldPassword;
    private TextView newPassword;
    private TextView newPasswordConf;
    private Button saveBtn;
    private int userID;

    public EditPasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences preferences = getActivity()
                .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        userID = preferences.getInt(Constant.USER_ID, 0);
        View rootView = inflater.inflate(R.layout.fragment_edit_password, container, false);
        oldPassword = rootView.findViewById(R.id.oldPassword);
        newPassword = rootView.findViewById(R.id.newPassword);
        newPasswordConf = rootView.findViewById(R.id.newPasswordConf);
        saveBtn = rootView.findViewById(R.id.saveNewPasswordBtn);
        saveNewPassword();
        return rootView;
    }

    private void saveNewPassword() {
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validFields()){
                    PasswordReset pass = new PasswordReset(userID,
                            oldPassword.getText().toString(),
                            newPassword.getText().toString());
                    new PasswordResetAsyncTask(getActivity(), pass).execute();
                    cleanValues();
                }
            }
        });
    }

    private void cleanValues() {
        oldPassword.setText("");
        newPasswordConf.setText("");
        newPassword.setText("");
    }

    private boolean validFields() {
        if(oldPassword.getText().toString().trim().equals("")
                || newPassword.getText().toString().trim().equals("")
                || newPasswordConf.getText().toString().trim().equals("")){
            Toast.makeText(getActivity(),
                    getActivity().getString(R.string.empty_fields), Toast.LENGTH_LONG).show();
            return false;
        }else if(!newPassword.getText().toString().trim()
                .equals(newPasswordConf.getText().toString().trim())){
            Toast.makeText(getActivity(),
                    getActivity().getString(R.string.pass_dont_match), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}
