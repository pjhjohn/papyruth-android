package com.papyruth.android.navigation_drawer;

/**
 * Created by pjhjohn on 2015-05-19.
 */
public class NavigationDrawerItem {
    private String text;
    private int drawableResourceId;
    public NavigationDrawerItem(String text, int drawableResourceId) {
        this.text = text;
        this.drawableResourceId = drawableResourceId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public void setDrawableResourceId(int resourceId) {
        this.drawableResourceId = resourceId;
    }

    public int getDrawableResourceId() {
        return this.drawableResourceId;
    }
}
