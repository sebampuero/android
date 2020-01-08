package com.example.tm18app.model;

import androidx.annotation.Nullable;

/**
 * Model that holds data of a comment
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class Comment implements Comparable<Comment>{

    private int id;
    private String content;
    private String name;
    private String lastname;
    private int userID;
    private int postID;
    private long timestamp;
    private String commentatorPicUrl;

    public Comment() {
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getUserID() {
        return userID;
    }

    public int getPostID() {
        return postID;
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setPostID(int postID) {
        this.postID = postID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCommentatorPicUrl() {
        return commentatorPicUrl;
    }

    public void setCommentatorPicUrl(String commentatorPicUrl) {
        this.commentatorPicUrl = commentatorPicUrl;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", name='" + name + '\'' +
                ", lastname='" + lastname + '\'' +
                ", userID=" + userID +
                ", postID=" + postID +
                ", timestamp=" + timestamp +
                ", commentatorPicUrl='" + commentatorPicUrl + '\'' +
                '}';
    }

    @Override
    public int compareTo(Comment comment) {
        return (int) (comment.getTimestamp() - this.timestamp);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Comment))
            return false;
        if (obj == this)
            return true;
        return this.getId() == ((Comment) obj).getId();
    }

    @Override
    public int hashCode() {
        return id;
    }
}
