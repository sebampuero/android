package com.example.tm18app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tm18app.R;

/**
 * A {@link DialogFragment} that contains the input to request new goal tags.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class NewGoalsDialogFragment extends DialogFragment {

    private EditText mGoalsEditText;
    private Button mSendRequestBtn;

    public NewGoalsDialogFragment() {

    }

    static NewGoalsDialogFragment newInstance(){
        return new NewGoalsDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_goal, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGoalsEditText = view.findViewById(R.id.goalsEditText);
        mSendRequestBtn = view.findViewById(R.id.sendGoalsReqBtn);
        mGoalsEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mSendRequestBtn.setOnClickListener(view1 -> {
            if(validInputs()){ // TODO: add real functionality
                Toast.makeText(getActivity(),
                        getActivity().getString(R.string.goal_request_toast_msg),
                        Toast.LENGTH_LONG).show();
                dismiss();
            }
        });
    }

    /**
     * Checks if the input fields is valid
     * @return true if input valid, false otherwise
     */
    private boolean validInputs() {
        if(mGoalsEditText.getText().toString().trim().contains(" ")){
            Toast.makeText(getActivity(),
                    getActivity().getString(R.string.goal_tip_msg),
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}
