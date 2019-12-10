package com.example.tm18app.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.tm18app.R;
import com.example.tm18app.databinding.FragmentEditPasswordBinding;
import com.example.tm18app.viewModels.MyViewModel;
import com.example.tm18app.viewModels.PasswordEditViewModel;

import java.net.HttpURLConnection;

/**
 * A simple {@link Fragment} subclass. Responsible for UI and events for the password edition UI.
 */
public class EditPasswordFragment extends Fragment {

    private MyViewModel mainModel;
    private PasswordEditViewModel model;

    public EditPasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        model = ViewModelProviders.of(this).get(PasswordEditViewModel.class);
        model.setContext(getContext());
        FragmentEditPasswordBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_edit_password, container, false);
        binding.setMyVM(model);
        binding.setLifecycleOwner(this);
        // Set observer for the status of the password change feedback
        model.getStatusCodeResponseLiveData().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer responseStatusCode) {
                handleResponse(responseStatusCode);
            }
        });
        return binding.getRoot();
    }

    /**
     * Handle the response from the server about the password change
     * @param responseStatusCode {@link Integer} that represents the HTTP Status Code of the response
     */
    private void handleResponse(Integer responseStatusCode) {
        if(responseStatusCode == HttpURLConnection.HTTP_INTERNAL_ERROR){
            Toast.makeText(getActivity(),
                    getActivity().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
        }else if(responseStatusCode == HttpURLConnection.HTTP_BAD_REQUEST){
            Toast.makeText(getActivity(),
                    getActivity().getString(R.string.old_password_error), Toast.LENGTH_SHORT).show();
        }else if(responseStatusCode == HttpURLConnection.HTTP_OK){
            Toast.makeText(getActivity(),
                    getActivity().getString(R.string.password_update_success), Toast.LENGTH_SHORT).show();
            // Navigate back to the edit profile UI
            mainModel.getNavController().navigateUp();
            // Reset the status code to 0 to prevent observer to be called again
            model.getStatusCodeResponseLiveData().setValue(0);
        }
    }


}
