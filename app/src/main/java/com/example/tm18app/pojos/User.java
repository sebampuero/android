package com.example.tm18app.pojos;

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

    public User() {

    }

    public User(int id, String name, String lastname, String email, String password, Integer[] goals) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.goals = goals;
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

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", goals=" + Arrays.toString(goals) +
                ", goalTags=" + Arrays.toString(goalTags) +
                '}';
    }
}
