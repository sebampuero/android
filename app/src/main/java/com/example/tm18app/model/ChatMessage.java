package com.example.tm18app.model;

import androidx.annotation.Nullable;

/**
 * Model that holds data of a chat message
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
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

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof ChatMessage))
            return false;
        if (obj == this)
            return true;
        return this.getChatId() == ((ChatMessage) obj).getChatId();
    }

    @Override
    public int hashCode() {
        return chatId;
    }
}
