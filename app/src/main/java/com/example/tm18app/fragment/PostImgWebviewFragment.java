package com.example.tm18app.fragment;


import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;
import com.example.tm18app.network.DownloadsManager;
import com.example.tm18app.network.NetworkConnectivity;

/**
 * A simple {@link BaseWebviewFragment}  subclass. This class loads post pictures
 *
 * @author Sebastian Ampuero
 * @version  1.0
 * @since 03.12.2019
 */
public class PostImgWebviewFragment extends BaseWebviewFragment {

    public static final String IMG_URL = "IMG_URL";
    public static final String IMG_NAME = "IMG_NAME";
    private String mImageUrl;
    private String mImageName;

    public PostImgWebviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mImageUrl = getArguments().getString(IMG_URL);
        mImageName = getArguments().getString(IMG_NAME);
        mToolbar.inflateMenu(R.menu.webview_menu); // this fragment loads a custom toolbar
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.downloadImg:
                        setDownload();
                        break;
                    case R.id.share:
                        setShareIntent();
                        break;
                }
                return false;
            }
        });
        loadPage();
        return view;
    }

    /**
     * Sets the share {@link Intent} for when the share icon is pressed.
     */
    private void setShareIntent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, mImageUrl);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    /**
     * Sets the download for when the download icon is pressed.
     */
    private void setDownload() {
        new DownloadsManager(mImageUrl, getContext())
                .setTitle(getResources().getString(R.string.downloading_img_msg))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setFilenameForImg(mImageName)
                .download();
    }

    @Override
    protected void loadPage() {
        mWebView.loadUrl(NetworkConnectivity.tweakImgQualityByNetworkType(getContext(), mImageUrl));
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
    }
}
