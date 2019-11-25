package com.example.tm18app.pojos;

public class Goal {

    private int id;
    private String tag;

    public Goal(int id, String tag) {
        this.id = id;
        this.tag = tag;
    }

    public Goal() {
    }

    public int getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
