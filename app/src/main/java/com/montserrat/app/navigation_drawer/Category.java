package com.montserrat.app.navigation_drawer;

/**
 * Created by pjhjohn on 2015-05-19.
 */
public class Category {
    private String text;
    private int resourceId;
    public Category(String text, int resourceId) {
        this.text = text;
        this.resourceId = resourceId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getResourceId() {
        return this.resourceId;
    }
}
