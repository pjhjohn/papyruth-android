package com.papyruth.android.navigation_drawer;

/**
 * Created by pjhjohn on 2015-05-19.
 */
public class NavigationDrawerItem {
    private String mLabel;
    private int mDrawableResourceId;
    public NavigationDrawerItem(String label, int resid) {
        mLabel = label;
        mDrawableResourceId = resid;
    }

    public void setLabel(String label) {
        mLabel = label;
    }
    public String getLabel() {
        return mLabel;
    }
    public void setDrawableResourceId(int resid) {
        mDrawableResourceId = resid;
    }
    public int getDrawableResourceId() {
        return mDrawableResourceId;
    }
}
