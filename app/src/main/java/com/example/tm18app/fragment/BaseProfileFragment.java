package com.example.tm18app.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.tm18app.R;
import com.example.tm18app.model.Post;

import java.util.List;

/**
 * A simple {@link Fragment} abstract subclass. This class is responsible for profile UI's
 *
 * @author Sebastian Ampuero
 * @version  1.0
 * @since 03.12.2019
 */
public abstract class BaseProfileFragment extends BasePostsContainerFragment {

    protected ProgressBar mProgressBar;
    protected LinearLayout mNoPostsView;
    protected ImageView mProfilePicIW;
    protected TextView mNamesTV;
    protected TextView mGoalsTvCall;
    protected String[] userGoals;
    protected ProgressBar mLoadMoreItemsProgressBar;

    protected View.OnClickListener goalsInfoClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            final AlertDialog dialog = builder.create();
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogLayout = inflater.inflate(R.layout.goals_layout, null);
            LinearLayout layout = dialogLayout.findViewById(R.id.goalsListContainer);
            dialog.setView(dialogLayout);
            dialog.show();
            dialog.setCancelable(true);
            for(String string : userGoals){
                TextView tv = new TextView(getContext());
                tv.setText(string);
                tv.setTextSize((float) 15);
                tv.setPadding(0,5,0,5);
                layout.addView(tv);
            }
        }
    };

    /**
     * Fetches profile pic and displays it
     */
    protected abstract void setProfilePic();

    /**
     * Fetches the {@link List} of {@link Post} items from the server.
     */
    protected abstract void fetchData();

}
