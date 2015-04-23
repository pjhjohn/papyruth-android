package com.montserrat.utils.viewpager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.montserrat.controller.AppConst;
import com.montserrat.parts.FragmentFactory;

import java.util.Stack;

/**
 * Created by pjhjohn on 2015-04-23.
 */
public class ViewPagerManager implements ViewPagerController {
    private FlexibleViewPager pager;
    private Stack<Integer> history;
    private boolean addToBackStack;
    private int currentPage;

    public ViewPagerManager (FlexibleViewPager pager, FragmentManager manager, FragmentFactory.Type fragmentType, int viewCount) {
        this.history = new Stack<Integer>();

        this.pager = pager;
        this.pager.setAdapter(new Adapter(manager, fragmentType, viewCount));
        this.pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled (int position, float positionOffset, int positionOffsetPixels) {}
            @Override public void onPageScrollStateChanged (int state) {}
            @Override
            public void onPageSelected (int position) {
                if(ViewPagerManager.this.addToBackStack) ViewPagerManager.this.history.push(Integer.valueOf(currentPage));
                ViewPagerManager.this.addToBackStack = true;
                ViewPagerManager.this.currentPage = position;
            }
        });
        this.addToBackStack = true;
        this.currentPage = this.pager.getCurrentItem();
    }

    public void setSwipeEnabled(boolean swipable) {
        this.pager.setSwipeEnabled(swipable);
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


    private static class Adapter extends FragmentStatePagerAdapter {
        private final FragmentFactory.Type type;
        private final int count;
        public Adapter(FragmentManager manager, FragmentFactory.Type fragmentType, int viewCount) {
            super(manager);
            this.type = fragmentType;
            this.count = viewCount;
        }

        @Override
        public Fragment getItem(int position) {
            return FragmentFactory.create(type, position);
        }

        @Override
        public int getCount() {
            return count;
        }
    }
}
