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
import com.example.tm18app.network.DownloadsManager;
import com.example.tm18app.network.NetworkConnectivity;
import com.example.tm18app.util.ConverterUtils;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass. Responsible for events and the mContent Image of a {@link com.example.tm18app.model.Post}
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class PostImgFragment extends BaseFragment implements MainActivity.GestureListener {

    public static final String IMG_URL = "img_url";
    // scale gesture for zoom functionality
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
        // Since a Gesture Detector can be only operated in the Activity containing a fragment,
        // we set the gesture listener for that activity
        ((MainActivity)requireActivity()).setGestureListener(this);
        return mRoot;
    }

    /**
     * Display the image into the {@link ImageView}
     */
    private void displayPostImage() {
        String url = NetworkConnectivity.tweakImgQualityByNetworkType(requireContext(),
                getArguments().getString(IMG_URL));
        String urlKey = ConverterUtils.extractUrlKey(url);
        Picasso.get()
                .load(url)
                .placeholder(R.drawable.placeholder)
                .stableKey(urlKey)
                .into(mImageView);
    }



    @Override
    protected void setupViews() {
        super.setupViews();
        mImageView = mRoot.findViewById(R.id.postImage);
        mToolbar.inflateMenu(R.menu.post_img_menu);
        mToolbar.setOnMenuItemClickListener(item -> {
            if(item.getItemId() ==  R.id.downloadImg){
                new DownloadsManager(getArguments().getString(IMG_URL), getContext())
                        .setFilenameForImg(String.valueOf(System.currentTimeMillis()))
                        .setNotificationVisibility(DownloadsManager.VISIBLE)
                        .setTitle(getString(R.string.downloading_img_msg))
                        .download();
            }
            return true;
        });
    }

    @Override
    public void onTouched(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
    }

    /**
     * Listener for a Touch Event that handles the scale for the zoom functionality
     */
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
