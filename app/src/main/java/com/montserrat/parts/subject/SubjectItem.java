package com.montserrat.parts.subject;

/**
 * Created by mrl on 2015-03-16.
 */
public class SubjectItem {
    String itemText;
    int itemImgResId;

    public SubjectItem(String itemText, int itemImgResId) {
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