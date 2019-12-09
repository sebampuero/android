package com.example.tm18app.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.R;
import com.example.tm18app.databinding.FragmentNewPostBinding;
import com.example.tm18app.viewModels.MyViewModel;
import com.example.tm18app.viewModels.NewPostViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass. Responsible for UI and events for the new post UI.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class NewPostFragment extends Fragment {

    private MyViewModel mainModel;
    private NewPostViewModel model;
    private FragmentNewPostBinding binding;
    private EditText postTitle;
    private EditText postContent;

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
        model.setContext(getContext());
        postContent = binding.inputTextEdit;
        postTitle = binding.postTitle;
        setSpinner();
        // set observer for new post response feedback
        model.getPostLiveDataResponse().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer statusCode) {
                evaluatePostResponse(statusCode);
            }
        });
        return binding.getRoot();
    }

    /**
     * Evaluate the status of the procedure of creating a new Post. Show feedback to the user.
     * @param statusCode {@link Integer} HTTP status code of the response from the server
     */
    private void evaluatePostResponse(Integer statusCode) {
        if(statusCode == 200){
            Toast.makeText(this.getContext(), this.getContext().getString(R.string.post_successfully_created), Toast.LENGTH_SHORT).show();
            mainModel.getNavController().navigateUp();
            cleanInputs();
        }
        else if(statusCode == 500) {
            Toast.makeText(this.getContext(), this.getContext().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
        }
        model.getPostLiveDataResponse().setValue(0);
    }

    /**
     * Empties the input fields for the Post
     */
    private void cleanInputs() {
        postTitle.setText("");
        postContent.setText("");
    }

    /**
     * Sets up the {@link Spinner} that contains the goal tags.
     */
    private void setSpinner() {
        Spinner goalTagsSpinner = binding.goalTagsSpinner;
        SharedPreferences preferences = getActivity().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        if(preferences.getString(Constant.GOAL_TAGS, null) != null){
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

}
