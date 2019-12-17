package com.example.tm18app.model;

public class ChatMessage implements Comparable<ChatMessage>{

    private int chatId;
    private long timestamp;
    private String text;
    private int roomId;
    private int senderId;

    public ChatMessage() {
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "chatId=" + chatId +
                ", timestamp=" + timestamp +
                ", text='" + text + '\'' +
                ", roomId=" + roomId +
                ", senderId=" + senderId +
                '}';
    }

    @Override
    public int compareTo(ChatMessage chatMessage) {
        return (int) (this.getTimestamp() - chatMessage.getTimestamp());
    }
}
