package com.example.tm18app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ProfileImgWebviewFragment extends BaseWebviewFragment {

    public static final String IMG_URL = "IMG_URL";
    private String mImageUrl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mImageUrl = getArguments().getString(IMG_URL);
        loadPage();
        return view;
    }

    @Override
    protected void loadPage() {
        mWebView.loadUrl(mImageUrl);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
    }
}
