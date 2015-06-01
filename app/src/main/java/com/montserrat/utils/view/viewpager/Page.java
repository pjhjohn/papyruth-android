package com.montserrat.utils.view.viewpager;

import com.montserrat.app.AppConst;

/**
 * Created by pjhjohn on 2015-05-31.
 */
public class Page {
    public AppConst.ViewPager.Type category = null;
    public int index;

    private Page(){}

    public static Page at(AppConst.ViewPager.Type category, int index) {
        Page info = new Page();
        info.category = category;
        info.index = index;
        return info;
    }

    @Override
    public boolean equals(Object another) {
        return
            another != null &&
            another instanceof Page &&
            this.category == ((Page) another).category &&
            this.index == ((Page) another).index;
    }

    @Override
    public String toString() {
        return String.format("%s#%d", AppConst.ViewPager.type2Str(this.category), index);
    }
}
