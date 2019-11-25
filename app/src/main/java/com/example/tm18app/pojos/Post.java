package com.example.tm18app.pojos;

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

    public Post() {
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
                '}';
    }


    @Override
    public int compareTo(Post post) {
        return (int) (post.getTimestamp() - this.timestamp);
    }
}
