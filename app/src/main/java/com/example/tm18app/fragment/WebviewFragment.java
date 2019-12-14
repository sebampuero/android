package com.example.tm18app.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class WebviewFragment extends Fragment {

    public static final String IMG_URL = "IMG_URL";

    private WebView mWebView;
    public WebviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_webview, container, false);
        mWebView = view.findViewById(R.id.webview);
        loadPage();
        return view;
    }

    private void loadPage() {
        String imageUrl = getArguments().getString(IMG_URL);
        ((MainActivity)getActivity()).getToolbar().setTitle(imageUrl);
        mWebView.loadUrl(imageUrl);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
    }

}
