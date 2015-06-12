package com.montserrat.utils.view.navigator;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by pjhjohn on 2015-06-12.
 */
public class NavigatableLinearLayout extends LinearLayout {
    public NavigatableLinearLayout(Context context) {
        super(context);
    }

    public NavigatableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NavigatableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public float getXFraction() {
        int width = ((Activity)this.getContext()).getWindowManager().getDefaultDisplay().getWidth();
        return (width == 0) ? 0 : getX() / (float) width;
    }

    public void setXFraction(float xFraction) {
        int width = ((Activity)this.getContext()).getWindowManager().getDefaultDisplay().getWidth();
        setX((width > 0) ? (xFraction * width) : 0);
    }
}
