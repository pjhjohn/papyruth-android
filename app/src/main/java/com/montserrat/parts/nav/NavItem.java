package com.montserrat.parts.nav;

/**
 * Created by mrl on 2015-03-16.
 */
public class NavItem {
    String itemText;
    int itemImgResId;

    public NavItem(String itemText, int itemImgResId) {
        super();
        this.itemText = itemText;
        this.itemImgResId = itemImgResId;
    }

    public String getItemText() {
        return itemText;
    }
    public void setItemText(String itemText) {
        this.itemText = itemText;
    }
    public int getItemImgResId() {
        return itemImgResId;
    }
    public void setItemImgResId(int itemImgResId) {
        this.itemImgResId = itemImgResId;
    }

}