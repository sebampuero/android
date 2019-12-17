package com.example.tm18app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;

/**
 * A simple {@link Fragment} abstract subclass. This class is responsible for loading pages inside
 * a {@link WebView}
 *
 * @author Sebastian Ampuero
 * @version  1.0
 * @since 03.12.2019
 */
public abstract class BaseWebviewFragment extends Fragment {


    protected WebView mWebView;
    protected Toolbar mToolbar;

    public BaseWebviewFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webview, container, false);
        mWebView = view.findViewById(R.id.webview);
        mToolbar = ((MainActivity)getActivity()).getToolbar();
        return view;
    }

    /**
     * Loads the page with a given URL in the {@link WebView}
     */
    protected abstract void loadPage();
}
