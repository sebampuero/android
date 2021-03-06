package com.example.tm18app.model;

/**
 * Model that holds data of a chat room
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class ChatRoom implements Comparable<ChatRoom>{

    private int receiverId;
    private String profilePic;
    private String receiverName;
    private int id;
    private String room;
    private long lastTimestamp;
    private int newMessageInRoom;
    private long lastOnline;

    public ChatRoom() {
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

    public int getNewMessageInRoom() {
        return newMessageInRoom;
    }

    public void setNewMessageInRoom(int newMessageInRoom) {
        this.newMessageInRoom = newMessageInRoom;
    }

    public long getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    @Override
    public String toString() {
        return "ChatRoom{" +
                "receiverId=" + receiverId +
                ", profilePic='" + profilePic + '\'' +
                ", receiverName='" + receiverName + '\'' +
                ", id=" + id +
                ", room='" + room + '\'' +
                ", lastTimestamp=" + lastTimestamp +
                ", newMessageInRoom=" + newMessageInRoom +
                ", lastOnline=" + lastOnline +
                '}';
    }

    @Override
    public int compareTo(ChatRoom chatRoom) {
        return (int) (chatRoom.getLastTimestamp() - this.getLastTimestamp());
    }
}
