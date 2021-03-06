package com.example.tm18app.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.tm18app.R;
import com.example.tm18app.databinding.FragmentEditPasswordBinding;
import com.example.tm18app.viewModels.PasswordEditViewModel;

import java.net.HttpURLConnection;

/**
 * A simple {@link Fragment} subclass. Responsible for UI and events for the mPassword edition UI.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class EditPasswordFragment extends BaseFragment {

    private PasswordEditViewModel mModel;

    public EditPasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mModel = ViewModelProviders.of(this).get(PasswordEditViewModel.class);
        mModel.setContext(getContext());
        FragmentEditPasswordBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_edit_password, container, false);
        binding.setMyVM(mModel);
        binding.setLifecycleOwner(this);
        // Set observer for the status of the mPassword change feedback
        mModel.getStatusCodeResponseLiveData().observe(this,
                this::handleResponse);
        setupViews();
        return binding.getRoot();
    }

    /**
     * Handle the response from the server about the mPassword change
     * @param responseStatusCode {@link Integer} that represents the HTTP Status Code of the response
     */
    private void handleResponse(Integer responseStatusCode) {
        if(responseStatusCode == HttpURLConnection.HTTP_INTERNAL_ERROR){
            Toast.makeText(requireActivity(),
                    getActivity().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
        }else if(responseStatusCode == HttpURLConnection.HTTP_BAD_REQUEST){
            Toast.makeText(requireActivity(),
                    getActivity().getString(R.string.old_password_error), Toast.LENGTH_SHORT).show();
        }else if(responseStatusCode == HttpURLConnection.HTTP_OK){
            Toast.makeText(requireActivity(),
                    getActivity().getString(R.string.password_update_success), Toast.LENGTH_SHORT).show();
            // Navigate back to the edit profile UI
            mMainModel.getNavController().navigateUp();
            // Reset the status code to 0 to prevent observer to be called again
            mModel.getStatusCodeResponseLiveData().setValue(0);
        }
    }


}
