package com.example.tm18app.model;

import java.util.Arrays;

/**
 * Model that holds data of a User
 *
 * @author Sebastian Ampueri
 * @version 1.0
 * @since 03.12.2019
 */
public class User {

   private int id;
   private String name;
   private String lastname;
   private String email;
   private String password;
   private Integer[] goals;
   private String[] goalTags;
   private String pushyToken;
   private String pushyAuthKey;
   private String profilePicUrl;
   private String base64ProfilePic;

    public User() {

    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public Integer[] getGoals() {
        return goals;
    }

    public void setGoals(Integer[] goals) {
        this.goals = goals;
    }

    public String[] getGoalTags() {
        return goalTags;
    }

    public void setGoalTags(String[] goalTags) {
        this.goalTags = goalTags;
    }

    public String getPushyToken() {
        return pushyToken;
    }

    public void setPushyToken(String pushyToken) {
        this.pushyToken = pushyToken;
    }

    public String getPushyAuthKey() {
        return pushyAuthKey;
    }

    public void setPushyAuthKey(String pushyAuthKey) {
        this.pushyAuthKey = pushyAuthKey;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getBase64ProfilePic() {
        return base64ProfilePic;
    }

    public void setBase64ProfilePic(String base64ProfilePic) {
        this.base64ProfilePic = base64ProfilePic;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", mName='" + name + '\'' +
                ", mLastname='" + lastname + '\'' +
                ", mEmail='" + email + '\'' +
                ", mPassword='" + password + '\'' +
                ", goals=" + Arrays.toString(goals) +
                ", goalTags=" + Arrays.toString(goalTags) +
                ", pushyToken='" + pushyToken + '\'' +
                ", pushyAuthKey='" + pushyAuthKey + '\'' +
                ", profilePicUrl='" + profilePicUrl + '\'' +
                ", base64ProfilePic='" + base64ProfilePic + '\'' +
                '}';
    }
}
