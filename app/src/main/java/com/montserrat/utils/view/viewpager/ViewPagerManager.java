package com.montserrat.utils.view.viewpager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.montserrat.app.AppConst.ViewPager.Type;
import com.montserrat.app.fragment.FragmentFactory;

import java.util.Stack;

/**
 * Created by pjhjohn on 2015-04-23.
 */
public class ViewPagerManager implements ViewPagerController {
    FlexibleViewPager pager;
    Stack<Integer> history;
    boolean addToBackStack;
    int current;
    int previous;
    Adapter adapter;
    ViewPager.OnPageChangeListener listener;

    public ViewPagerManager (FlexibleViewPager pager, FragmentManager manager, Type type, int viewCount) {
        this(pager, manager, type, viewCount, null);
    }
    public ViewPagerManager (FlexibleViewPager pager, FragmentManager manager, Type type, int viewCount, ViewPager.SimpleOnPageChangeListener container) {
        this.pager = pager;
        this.adapter = new Adapter(manager, type, viewCount);
        this.listener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected (int position) {
                if (ViewPagerManager.this.addToBackStack) ViewPagerManager.this.history.push(current);
                ViewPagerManager.this.addToBackStack = true;
                ViewPagerManager.this.previous = ViewPagerManager.this.current;
                ViewPagerManager.this.current = position;

                final Fragment target = ViewPagerManager.this.adapter.getFragmentAt(position);
                if (container != null) container.onPageSelected(position);
                if (target != null && target.getView() != null && target instanceof OnPageFocus) ((OnPageFocus) target).onPageFocused();
            }
        };
        this.active();
    }

    public void active() {
        this.pager.setAdapter(this.adapter);
        this.pager.setOnPageChangeListener(this.listener);
        this.reset();
    }

    public void reset() {
        this.history = new Stack<>();
        this.addToBackStack = true;
        this.current = 0;
        this.setCurrentPage(this.current, false);
    }

    @Override
    public Stack<Integer> getHistoryCopy() {
        return (Stack<Integer>) this.history.clone();
    }

    @Override
    public int getPreviousPage() {
        return this.previous;
    }

    @Override
    public void setCurrentPage(int pageNum, boolean addToBackStack) {
        this.addToBackStack = addToBackStack;
        this.pager.setCurrentItem(pageNum);
    }

    @Override
    public boolean popCurrentPage() {
        if(!this.history.isEmpty()){
            final Integer popped = this.history.pop();
            this.addToBackStack = false;
            this.pager.setCurrentItem(popped);
            return true;
        } return false;
    }

    @Override
    public boolean onBack() {
        final Fragment target = ViewPagerManager.this.adapter.getFragmentAt(this.current);
        if (target != null) {
            boolean backed = false;
            if(target instanceof OnBack) backed = ((OnBack)target).onBack();
            if(!backed) return popCurrentPage();
            else return true;
        } return false;
    }

    static class Adapter extends FragmentStatePagerAdapter {
        private SparseArray<Fragment> fragments;
        private final Type type;
        private final int count;
        public Adapter(FragmentManager manager, Type type, int viewCount) {
            super(manager);
            this.type = type;
            this.count = viewCount;
            this.fragments = new SparseArray<>();
        }

        @Override
        public Fragment getItem(int position) {
            return FragmentFactory.create(type, position);
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            this.fragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            fragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getFragmentAt (int position) {
            return this.fragments.get(position);
        }
    }
}
