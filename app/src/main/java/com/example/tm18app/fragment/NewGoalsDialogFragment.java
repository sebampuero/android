package com.example.tm18app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tm18app.R;

public class NewGoalsDialogFragment extends DialogFragment {

    private EditText goalsText;
    private Button sendReqBtn;

    public NewGoalsDialogFragment() {

    }

    public static NewGoalsDialogFragment newInstance(){
        return new NewGoalsDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_goal, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        goalsText = view.findViewById(R.id.goalsEditText);
        sendReqBtn = view.findViewById(R.id.sendGoalsReqBtn);
        goalsText.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        sendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validInputs()){
                    Toast.makeText(getActivity(),
                            getActivity().getString(R.string.goal_request_toast_msg),
                            Toast.LENGTH_LONG).show();
                    dismiss();
                }
            }
        });
    }

    private boolean validInputs() {
        if(goalsText.getText().toString().trim().contains(" ")){
            Toast.makeText(getActivity(),
                    getActivity().getString(R.string.goal_tip_msg),
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}
