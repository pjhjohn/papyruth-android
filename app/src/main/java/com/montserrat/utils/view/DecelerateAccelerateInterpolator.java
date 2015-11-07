package com.montserrat.utils.view;

import android.view.animation.Interpolator;

/**
 * Created by pjhjohn on 2015-11-07.
 */
public class DecelerateAccelerateInterpolator implements Interpolator {
    private final float mFactor;

    public DecelerateAccelerateInterpolator() {
        mFactor = 1.0f;
    }

    public DecelerateAccelerateInterpolator(float factor) {
        mFactor = factor;
    }

//    x > 0.5 : 0.5+0.5*(2*(x-0.5))^(1/factor)
//    x < 0.5 : 0.5-0.5*(-2*(x-0.5))^(1/factor)
//    x = 0.5 : 0.5
    public float getInterpolation(float x) {
        float y = 0.5f;
        if(x > 0.5f) y += 0.5f * (float)Math.pow(2.0f * (x - 0.5f), 1.0f/mFactor);
        else if(x < 0.5f) y -= 0.5f * (float)Math.pow(-2.0f * (x - 0.5f), 1.0f/mFactor);
        return y;
    }
}
