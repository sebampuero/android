package com.example.tm18app.fragment;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

abstract class CustomScrollListener extends RecyclerView.OnScrollListener {

    final String TAG = getClass().getSimpleName();

    LinearLayoutManager layoutManager;

    CustomScrollListener(LinearLayoutManager manager){
        this.layoutManager = manager;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if(!recyclerView.canScrollVertically(1)
                && newState == RecyclerView.SCROLL_STATE_IDLE){
            loadMoreItems();
        }
    }

    abstract void loadMoreItems();
}
