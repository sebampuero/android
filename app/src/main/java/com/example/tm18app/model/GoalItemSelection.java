package com.example.tm18app.model;

import java.io.Serializable;

/**
 * Model that holds data for a GoalItemSelection
 * @see com.example.tm18app.adapters.MultiGoalSelectAdapter
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class GoalItemSelection implements Serializable {

    private String tag;
    private int id;
    private boolean isChecked;

    public GoalItemSelection(String tag, boolean isChecked, int id) {
        this.tag = tag;
        this.isChecked = isChecked;
        this.id = id;
    }

    public GoalItemSelection() {
    }

    public String getTag() {
        return tag;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "GoalItemSelection{" +
                "tag='" + tag + '\'' +
                ", id=" + id +
                ", isChecked=" + isChecked +
                '}';
    }
}
