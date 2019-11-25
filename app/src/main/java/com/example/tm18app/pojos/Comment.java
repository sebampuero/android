package com.example.tm18app.pojos;

public class Comment implements Comparable<Comment>{

    private int id;
    private String content;
    private String name;
    private String lastname;
    private int userID;
    private int postID;
    private long timestamp;

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

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", name='" + name + '\'' +
                ", lastname='" + lastname + '\'' +
                ", userID=" + userID +
                ", postID=" + postID +
                '}';
    }

    @Override
    public int compareTo(Comment comment) {
        return (int) (comment.getTimestamp() - this.timestamp);
    }
}
