package com.papyruth.utils.view;

import android.view.animation.Interpolator;

/**
 * Quadratic Interpolator <br>
 * AccelerateDecelerate for mFactor > 1<br>
 * Linear for mFactor = 1<br>
 * DecelerateAccelerate for 0 < mFactor < 1
 */
public class ExponentialInterpolator implements Interpolator {
    private final float mFactor;

    public ExponentialInterpolator() {
        mFactor = 1.0f;
    }

    public ExponentialInterpolator(float factor) {
        mFactor = factor;
    }

    /**
     * @param x input for interpolator btw 0 and 1
     * @return value of interpolation point btw 0 and 1<br>
     * x > 0.5 : 0.5+0.5*(2*(x-0.5))^(factor)<br>
     * x < 0.5 : 0.5-0.5*(-2*(x-0.5))^(factor)<br>
     * x = 0.5 : 0.5
     */
    @Override
    public float getInterpolation(float x) {
        float y = 0.5f;
        if(x > 0.5f) y += 0.5f * (float)Math.pow(2.0f * (x - 0.5f), mFactor);
        else if(x < 0.5f) y -= 0.5f * (float)Math.pow(-2.0f * (x - 0.5f), mFactor);
        return y;
    }
}
