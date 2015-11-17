package com.papyruth.utils.view.viewpager;

import java.util.Stack;

/**
 * Created by pjhjohn on 2015-04-17.
 */
public interface ViewPagerController {
    Stack<Integer> getHistoryCopy();
    int getPreviousPage();
    void setCurrentPage(int pageNum, boolean addToBackStack);
    boolean popCurrentPage();
    boolean back();
    void addImeControlFragment(int page);
    boolean controlTargetContains(int number);
    int getCurrentPage();
}
