package com.montserrat.utils.view.viewpager;

import java.util.Stack;

/**
 * Created by pjhjohn on 2015-04-17.
 */
public interface ViewPagerContainerController {
    Stack<Page> getHistoryCopy();
    Page getPreviousPage();
    void setCurrentPage(Page page, boolean addToBackStack);
    boolean popCurrentPage();
    boolean onBack();
}
