package com.example.tm18app.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tm18app.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FullScreenImgFragment extends Fragment {


    public FullScreenImgFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_full_screen_img, container, false);
    }

}
