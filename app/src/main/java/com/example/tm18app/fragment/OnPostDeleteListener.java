package com.example.tm18app.fragment;

import androidx.lifecycle.MutableLiveData;

/**
 * Listener interface for deletion of {@link com.example.tm18app.pojos.Post} items
 *
 * @author Sebastian Ampuero
 * @version  1.0
 * @since 03.12.2019
 */
public interface OnPostDeleteListener {

    /**
     * Listener method for when a Post is deleted
     * @param statusCode {@link MutableLiveData} that represents changes of the response's status
     *                                          code.
     */
    void onPostDeleted(MutableLiveData<Integer> statusCode);

}
