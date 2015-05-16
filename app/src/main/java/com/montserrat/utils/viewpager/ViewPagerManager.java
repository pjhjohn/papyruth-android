package com.montserrat.utils.viewpager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.montserrat.app.fragment.FragmentFactory;

import java.util.Stack;

import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-23.
 */
public class ViewPagerManager implements ViewPagerController {
    public static enum Mode {
        STANDARD, SWIPE, MULTIPLE, SWIPE_MULTIPLE
    }
    private FlexibleViewPager pager;
    private Stack<Integer> history;
    private boolean addToBackStack;
    private int currentPage;
    private Adapter adapter;
    private ViewPager.OnPageChangeListener listener;

    public ViewPagerManager (FlexibleViewPager pager, FragmentManager manager, final FragmentFactory.Type fragmentType, int viewCount) {
        this.pager = pager;
        this.adapter = new Adapter(manager, fragmentType, viewCount);
        this.listener = new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled (int position, float positionOffset, int positionOffsetPixels) {}
            @Override public void onPageScrollStateChanged (int state) {}
            @Override
            public void onPageSelected (int position) {
                Timber.d("onPage#%dSelected", position);
                if (ViewPagerManager.this.addToBackStack) ViewPagerManager.this.history.push(currentPage);
                ViewPagerManager.this.addToBackStack = true;
                ViewPagerManager.this.currentPage = position;

                Fragment item = manager.findFragmentByTag("android:switcher:" + ViewPagerManager.this.pager.getId() + ":" + position);
                if (item != null && item.getView() != null && item instanceof  OnPageFocus)
                    ((OnPageFocus) item).onPageFocused();

//                final Fragment target = ViewPagerManager.this.adapter.getFragmentAt(position);
//                if(target instanceof OnPageFocus) ((OnPageFocus) target).onPageFocused();
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
        this.currentPage = 0;
        this.setCurrentPage(this.currentPage, false);
    }

    public void setMode(Mode mode) {
        switch(mode) {
            case STANDARD:
                this.setSwipeEnabled(false);
                this.setAdjacentPagesVisible(false);
            case SWIPE:
                this.setSwipeEnabled(true);
                this.setAdjacentPagesVisible(false);
            case MULTIPLE:
                this.setSwipeEnabled(false);
                this.setAdjacentPagesVisible(true);
            case SWIPE_MULTIPLE:
                this.setSwipeEnabled(true);
                this.setAdjacentPagesVisible(true);
        }
    }

    public void setSwipeEnabled(boolean swipable) {
        this.pager.setSwipeEnabled(swipable);
    }
    
    public void setAdjacentPagesVisible(boolean visible){
        this.pager.setPadding(
            visible ? 20 : 0,
            0,
            visible ? 20 : 0,
            0
        );
        this.pager.setClipToPadding(visible);
    }

    @Override
    public void setCurrentPage(int pageNum, boolean addToBackStack) {
        Timber.d("setCurrentPage#%d", pageNum);
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

    public Fragment getFragment(int position) {
        return this.adapter.getFragmentAt(position);
    }


    private static class Adapter extends FragmentStatePagerAdapter {
        private SparseArray<Fragment> fragments;
        private final FragmentFactory.Type type;
        private final int count;
        public Adapter(FragmentManager manager, FragmentFactory.Type fragmentType, int viewCount) {
            super(manager);
            this.type = fragmentType;
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
