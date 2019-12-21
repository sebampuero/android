package com.example.tm18app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.viewModels.MyViewModel;

public abstract class BaseFragment extends Fragment {
    protected MyViewModel mMainModel;
    protected SharedPreferences mPrefs;

    public BaseFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainModel = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        mPrefs = getContext().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
    }

    protected abstract void setupViews();

}
