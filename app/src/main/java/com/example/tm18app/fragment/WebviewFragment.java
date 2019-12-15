package com.example.tm18app.fragment;


import android.app.DownloadManager;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class WebviewFragment extends Fragment {

    public static final String IMG_URL = "IMG_URL";
    public static final String IMG_NAME = "IMG_NAME";

    private WebView mWebView;
    private Toolbar mToolbar;
    private String mImageUrl;
    private String mImageName;

    public WebviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_webview, container, false);
        mImageUrl = getArguments().getString(IMG_URL);
        mImageName = getArguments().getString(IMG_NAME);
        mWebView = view.findViewById(R.id.webview);
        mToolbar = view.findViewById(R.id.toolbarWebView);
        mToolbar.inflateMenu(R.menu.webview_menu);
        ((MainActivity)getActivity()).getToolbar().setVisibility(View.GONE);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigateUp();
            }
        });
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.downloadImg:
                        setDownload();
                        break;
                    case R.id.share:
                        //TODO: add something
                        break;
                }
                return false;
            }
        });
        loadPage();
        return view;
    }

    private void setDownload() {
        Toast.makeText(getContext(),
                getResources().getString(R.string.downloading_img_msg), Toast.LENGTH_SHORT).show();
        new DownloadsManager(mImageUrl, getContext())
                .setTitle(getResources().getString(R.string.downloading_img_msg))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setFilenameForImg(mImageName)
                .download();
    }

    private void loadPage() {
        mWebView.loadUrl(mImageUrl);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
    }

}
