package com.example.tm18app.model;

/**
 * Model that holds data of a Post
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class Post implements Comparable<Post>{

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
    private String posterPicUrl;
    private String contentPicUrl;

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

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", setTitle='" + title + '\'' +
                ", content='" + content + '\'' +
                ", userID=" + userID +
                ", name='" + name + '\'' +
                ", lastname='" + lastname + '\'' +
                ", goalTag='" + goalTag + '\'' +
                ", goalId=" + goalId +
                ", commentCount=" + commentCount +
                ", timestamp=" + timestamp +
                ", base64Image='" + base64Image + '\'' +
                ", posterPicUrl='" + posterPicUrl + '\'' +
                ", contentPicUrl='" + contentPicUrl + '\'' +
                '}';
    }

    @Override
    public int compareTo(Post post) {
        return (int) (post.getTimestamp() - this.timestamp);
    }
}
