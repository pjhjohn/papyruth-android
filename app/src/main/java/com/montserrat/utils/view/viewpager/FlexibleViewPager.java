package com.montserrat.utils.view.viewpager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-17.
 */
public class FlexibleViewPager extends ViewPager {
    private boolean mIsEnabledSwipe = false;
    private ViewPagerController pagerController;

    public FlexibleViewPager (Context context) {
        super(context);
    }

    public FlexibleViewPager (Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        if (!mIsEnabledSwipe) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent (MotionEvent event) {
        if (!mIsEnabledSwipe) {
            return false;
        }
        return super.onInterceptTouchEvent(event);
    }

    public void setSwipeEnabled (boolean enabled) {
        mIsEnabledSwipe = enabled;
    }

    public void setPagerController(ViewPagerController pagerController){
        this.pagerController = pagerController;
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {

        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            Timber.d("back from pager");
            if (this.pagerController.back())
                return true;
        }
        return super.dispatchKeyEventPreIme(event);
    }
}
