package com.example.tm18app.model;

import androidx.annotation.Nullable;

/**
 * Model that holds data of a Post
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class Post implements Comparable<Post> {

    private int id;
    private String title;
    private String content;
    private int userID;
    private String name;
    private String lastname;
    private String goalTag;
    private int goalId;
    private int commentCount;
    private long timestamp;
    private String base64Image;
    private String base64Video;
    private String posterPicUrl;
    private String contentPicUrl;
    private String contentVideoUrl;
    private String contentVideoThumbnailUrl;
    private String subscriberIds;

    private String contentVideoURI;
    private String contentImageURI;

    public Post() {
    }

    public Post(String title, String content, int userID, int goalID) {
        this.title = title;
        this.content = content;
        this.userID = userID;
        this.goalId = goalID;
    }


    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getUserID() {
        return userID;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getGoalTag() {
        return goalTag;
    }

    public void setGoalTag(String goalTag) {
        this.goalTag = goalTag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
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

    public void setGoalId(int goalId) {
        this.goalId = goalId;
    }

    public int getGoalId() {
        return goalId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPosterPicUrl() {
        return posterPicUrl;
    }

    public void setPosterPicUrl(String posterPicUrl) {
        this.posterPicUrl = posterPicUrl;
    }

    public String getContentPicUrl() {
        return contentPicUrl;
    }

    public void setContentPicUrl(String contentPicUrl) {
        this.contentPicUrl = contentPicUrl;
    }

    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }

    public String getSubscriberIds() {
        return subscriberIds;
    }

    public void setSubscriberIds(String subscriberIds) {
        this.subscriberIds = subscriberIds;
    }

    public String getBase64Video() {
        return base64Video;
    }

    public void setBase64Video(String base64Video) {
        this.base64Video = base64Video;
    }

    public String getContentVideoUrl() {
        return contentVideoUrl;
    }

    public void setContentVideoUrl(String contentVideoUrl) {
        this.contentVideoUrl = contentVideoUrl;
    }

    public String getContentVideoURI() {
        return contentVideoURI;
    }

    public void setContentVideoURI(String contentVideoURI) {
        this.contentVideoURI = contentVideoURI;
    }

    public String getContentImageURI() {
        return contentImageURI;
    }

    public void setContentImageURI(String contentImageURI) {
        this.contentImageURI = contentImageURI;
    }

    public String getContentVideoThumbnailUrl() {
        return contentVideoThumbnailUrl;
    }

    public void setContentVideoThumbnailUrl(String contentVideoThumbnailUrl) {
        this.contentVideoThumbnailUrl = contentVideoThumbnailUrl;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", userID=" + userID +
                ", name='" + name + '\'' +
                ", lastname='" + lastname + '\'' +
                ", goalTag='" + goalTag + '\'' +
                ", goalId=" + goalId +
                ", commentCount=" + commentCount +
                ", timestamp=" + timestamp +
                ", posterPicUrl='" + posterPicUrl + '\'' +
                ", contentPicUrl='" + contentPicUrl + '\'' +
                ", contentVideoUrl='" + contentVideoUrl + '\'' +
                ", contentVideoThumbnailUrl='" + contentVideoThumbnailUrl + '\'' +
                ", subscriberIds='" + subscriberIds + '\'' +
                ", contentVideoURI='" + contentVideoURI + '\'' +
                ", contentImageURI='" + contentImageURI + '\'' +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Post))
            return false;
        if (obj == this)
            return true;
        return this.getId() == ((Post) obj).getId();
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public int compareTo(Post post) {
        return (int) (post.getTimestamp() - this.timestamp);
    }
}
