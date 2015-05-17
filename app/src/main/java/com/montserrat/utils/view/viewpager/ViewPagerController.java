package com.montserrat.utils.view.viewpager;

/**
 * Created by pjhjohn on 2015-04-17.
 */
public interface ViewPagerController {
    int getPreviousPage();
    void setCurrentPage(int pageNum, boolean addToBackStack);
    void popCurrentPage();
}
