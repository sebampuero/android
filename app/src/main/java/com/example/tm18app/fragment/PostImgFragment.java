package com.example.tm18app.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;
import com.example.tm18app.network.NetworkConnectivity;
import com.example.tm18app.util.ConverterUtils;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostImgFragment extends BaseFragment implements MainActivity.GestureListener {

    public static final String IMG_URL = "img_url";

    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;
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
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        displayPostImage();
        ((MainActivity)getActivity()).setGestureListener(this);
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

    @Override
    public void onTouched(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f,
                    Math.min(mScaleFactor, 10.0f));
            mImageView.setScaleX(mScaleFactor);
            mImageView.setScaleY(mScaleFactor);
            return true;
        }
    }
}
