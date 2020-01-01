package com.example.tm18app.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.tm18app.R;
import com.example.tm18app.network.NetworkConnectivity;
import com.example.tm18app.util.ConverterUtils;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostImgFragment extends BaseFragment {

    public static final String IMG_URL = "img_url";

    private ImageView mImageView;
    private View mRoot;

    public PostImgFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_post_img, container, false);
        setupViews();
        displayPostImage();
        return mRoot;
    }

    private void displayPostImage() {
        String url = NetworkConnectivity.tweakImgQualityByNetworkType(getContext(),
                getArguments().getString(IMG_URL));
        String urlKey = ConverterUtils.extractUrlKey(url);
        Picasso.get()
                .load(url)
                .placeholder(R.drawable.progress_img_animation)
                .stableKey(urlKey)
                .into(mImageView);
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        mImageView = mRoot.findViewById(R.id.postImage);
    }
}
