package com.example.tm18app.viewModels;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.model.Comment;
import com.example.tm18app.repository.PostItemRepository;

import java.util.List;

/**
 * A {@link ViewModel} class representing the ViewModel for the {@link com.example.tm18app.fragment.CommentSectionFragment} View
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class CommentsSectionViewModel extends ViewModel {

    public MutableLiveData<String> mInputComment = new MutableLiveData<>();

    private Context mContext;
    private String mPostID;
    private MutableLiveData<List<Comment>> mCommentLiveData;

    /**
     * Getter for the {@link MutableLiveData} that reveals changes for the status of a comment creation
     * @return {@link MutableLiveData}
     */
    public MutableLiveData<List<Comment>> getCommentLiveData() {
        return mCommentLiveData;
    }

    /**
     * Event method that gets called when a comment is created
     */
    public void onPostComment() {
        if(mInputComment.getValue() != null){
            if(!mInputComment.getValue().trim().equals("")){
                SharedPreferences preferences = mContext
                        .getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
                PostItemRepository postItemRepository = new PostItemRepository();
                Comment comment = new Comment();
                comment.setContent(mInputComment.getValue());
                comment.setPostID(Integer.parseInt(mPostID));
                comment.setUserID(preferences.getInt(Constant.USER_ID, 0));
                postItemRepository.createComment(comment,
                        mCommentLiveData,
                        preferences.getString(Constant.PUSHY_TOKEN, ""));
            }
        }
    }

    /**
     * Sets the {@link Context} for this ViewModel
     * @param appContext {@link Context}
     */
    public void setAppContext(Context appContext) {
        this.mContext = appContext;
        callRepository();
    }

    /**
     * Calls the {@link PostItemRepository} and fetches the data from the server
     */
    private void callRepository() {
        SharedPreferences prefs =
                mContext.getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
        PostItemRepository postItemRepository = new PostItemRepository();
        this.mCommentLiveData = postItemRepository.getComments(mPostID,
                prefs.getString(Constant.PUSHY_TOKEN, ""));
    }

    /**
     * Sets the post id this comment belongs to
     * @param postID {@link String}
     */
    public void setPostID(String postID) {
        this.mPostID = postID;
    }

}
