package com.example.tm18app.fragment;


import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Custom scroll listener for {@link RecyclerView} containing {@link com.example.tm18app.model.Post}
 * items. Responsible for managing pagination for UI's that display a list of Posts.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 10.12.2019
 */
abstract class CustomScrollListener extends RecyclerView.OnScrollListener {

    final String TAG = getClass().getSimpleName();

    LinearLayoutManager layoutManager;

    CustomScrollListener(LinearLayoutManager manager){
        this.layoutManager = manager;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if(!isLoading() && !lastPageReached()){
            if(!recyclerView.canScrollVertically(1) // user stopped scrolling and view reached
                    // end of window
                    && newState == RecyclerView.SCROLL_STATE_IDLE ){
                loadMoreItems();
            }
        }
    }

    /**
     * On a page end, load more items from the db
     */
    abstract void loadMoreItems();

    /**
     * Whether items are being loaded or not
     * @return true if currently loading, false otherwise
     */
    abstract boolean isLoading();

    /**
     * If last page is reached
     * @return if last page is reached
     */
    abstract boolean lastPageReached();

}
