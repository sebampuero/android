package com.example.tm18app.pojos;

public class PasswordReset {

    private int userID;
    private String oldPassword;
    private String newPassword;

    public PasswordReset(int userID, String oldPassword, String newPassword) {
        this.userID = userID;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
