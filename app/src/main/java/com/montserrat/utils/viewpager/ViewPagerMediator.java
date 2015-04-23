package com.montserrat.utils.viewpager;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import java.util.Stack;

/**
 * Created by pjhjohn on 2015-04-23.
 */
public class ViewPagerMediator implements ViewPagerController {
    private ViewPager pager;
    private PagerAdapter adapter;
    private Stack<Integer> history;
    private boolean addToBackStack;
    private int currentPage;
    public ViewPagerMediator(ViewPager pager, PagerAdapter adapter) {
        this.history = new Stack<Integer>();
        this.adapter = adapter;
        this.pager = pager;
        this.pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled (int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected (int position) {
                if(ViewPagerMediator.this.addToBackStack) ViewPagerMediator.this.history.push(Integer.valueOf(currentPage));
                ViewPagerMediator.this.addToBackStack = true;
                ViewPagerMediator.this.currentPage = position;
            }

            @Override
            public void onPageScrollStateChanged (int state) {

            }
        });
        this.addToBackStack = true;
        this.currentPage = this.pager.getCurrentItem();
    }

    @Override
    public void setCurrentPage(int pageNum, boolean addToBackStack) {
        this.addToBackStack = addToBackStack;
        this.pager.setCurrentItem(pageNum);
        Log.d("DEBUG", "page set to #" + pageNum + ", history length : " + this.history.size());
    }

    /**
     * @return true if override is succeed, false otherwise.
     */
    public boolean onBackPressed() {
        Log.d("DEBUG", "onBackPressed on Mediator");
        if(!this.history.isEmpty()) {
            this.addToBackStack = false;
            this.pager.setCurrentItem(this.history.pop().intValue());
            return true;
        } return false;
    }
}
