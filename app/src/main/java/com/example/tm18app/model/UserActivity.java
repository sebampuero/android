package com.example.tm18app.model;

public class UserActivity {

    private boolean alreadyChecked;

    private boolean chatActivity;

    public UserActivity() {
    }

    public boolean isChatActivity() {
        return chatActivity;
    }

    public void setChatActivity(boolean chatActivity) {
        this.chatActivity = chatActivity;
    }

    public boolean isAlreadyChecked() {
        return alreadyChecked;
    }

    public void setAlreadyChecked(boolean alreadyChecked) {
        this.alreadyChecked = alreadyChecked;
    }
}
