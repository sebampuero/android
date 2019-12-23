package com.example.tm18app.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.PostCardviewBinding;
import com.example.tm18app.fragment.CommentSectionFragment;
import com.example.tm18app.fragment.FeedFragment;
import com.example.tm18app.fragment.OtherProfileFragment;
import com.example.tm18app.fragment.ProfileFragment;
import com.example.tm18app.fragment.PostWebViewFragment;
import com.example.tm18app.network.NetworkConnectivity;
import com.example.tm18app.model.Post;
import com.example.tm18app.repository.PostItemRepository;
import com.example.tm18app.util.TimeUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Adapter for the post items in Feed and Profile
 * @see com.example.tm18app.fragment.FeedFragment
 * @see com.example.tm18app.fragment.ProfileFragment
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class PostItemAdapter extends RecyclerView.Adapter<PostViewHolder> {
    private ArrayList<Post> mPostsList;
    private NavController mNavController;
    private Fragment mCurrentFragment;
    private SharedPreferences mPrefs;

    public interface OnPostDeleteListener {

        /**
         * Listener method for when a Post is deleted
         * @param statusCode {@link MutableLiveData} that represents changes of the response's status
         *                                          code.
         */
        void onPostDeleted(MutableLiveData<Integer> statusCode);

    }

    public PostItemAdapter(ArrayList<Post> posts, NavController mNavController, Fragment fragment) {
        this.mPostsList = posts;
        this.mNavController = mNavController;
        this.mCurrentFragment = fragment;
        mPrefs = mCurrentFragment.getContext().getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        PostCardviewBinding itemBinding = PostCardviewBinding.inflate(layoutInflater, parent, false);
        return new PostViewHolder(itemBinding, mCurrentFragment, mPostsList, mNavController, mPrefs, this);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.onBind(mPostsList.get(position));
    }

    @Override
    public int getItemCount() {
        return (mPostsList != null) ? mPostsList.size() : 0;
    }

}
