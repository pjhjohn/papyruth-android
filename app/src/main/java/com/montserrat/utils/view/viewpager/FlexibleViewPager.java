package com.montserrat.utils.view.viewpager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by pjhjohn on 2015-04-17.
 */
public class FlexibleViewPager extends ViewPager {
    private boolean mIsEnabledSwipe = true;

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
}
