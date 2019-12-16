package com.example.tm18app.model;

public class Chat implements Comparable<Chat>{

    private int receiverId;
    private String profilePic;
    private String receiverName;
    private int id;
    private String room;
    private long lastTimestamp;

    public Chat() {
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "receiverId=" + receiverId +
                ", profilePic='" + profilePic + '\'' +
                ", receiverName='" + receiverName + '\'' +
                ", id=" + id +
                ", room='" + room + '\'' +
                ", lastTimestamp=" + lastTimestamp +
                '}';
    }

    @Override
    public int compareTo(Chat chat) {
        return (int) (chat.getLastTimestamp() - this.getLastTimestamp());
    }
}
