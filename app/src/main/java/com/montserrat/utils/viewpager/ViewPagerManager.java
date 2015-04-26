package com.montserrat.utils.viewpager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

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
    private Adapter adapter;
    private ViewPager.OnPageChangeListener listener;

    public ViewPagerManager (FlexibleViewPager pager, FragmentManager manager, FragmentFactory.Type fragmentType, int viewCount) {
        this.adapter = new Adapter(manager, fragmentType, viewCount);
        this.listener = new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled (int position, float positionOffset, int positionOffsetPixels) {}
            @Override public void onPageScrollStateChanged (int state) {}
            @Override
            public void onPageSelected (int position) {
                if(ViewPagerManager.this.addToBackStack) ViewPagerManager.this.history.push(currentPage);
                ViewPagerManager.this.addToBackStack = true;
                ViewPagerManager.this.currentPage = position;
            }
        };

        this.pager = pager;
        this.active();
    }

    public void active() {
        this.pager.setAdapter(this.adapter);
        this.pager.setOnPageChangeListener(this.listener);
        this.reset();
    }

    public void reset() {
        this.history = new Stack<Integer>();
        this.addToBackStack = true;
        this.currentPage = 0;
        this.setCurrentPage(this.currentPage, false);
    }

    public void setSwipeEnabled(boolean swipable) {
        this.pager.setSwipeEnabled(swipable);
    }

    @Override
    public void setCurrentPage(int pageNum, boolean addToBackStack) {
        this.addToBackStack = addToBackStack;
        this.pager.setCurrentItem(pageNum);
    }

    /**
     * @return true if override is succeed, false otherwise.
     */
    public boolean onBackPressed() {
        if(!this.history.isEmpty()) {
            this.addToBackStack = false;
            this.pager.setCurrentItem(this.history.pop());
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
