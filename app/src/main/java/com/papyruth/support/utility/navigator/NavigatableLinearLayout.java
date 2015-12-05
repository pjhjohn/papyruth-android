package com.papyruth.support.utility.navigator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
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
        int width = this.getContext().getResources().getDisplayMetrics().widthPixels;
        return (width == 0) ? 0 : getX() / (float) width;
    }

    public void setXFraction(float xFraction) {
        int width = this.getContext().getResources().getDisplayMetrics().widthPixels;
        setX((width > 0) ? (xFraction * width) : 0);
    }

    public float getYFraction() {
        int height = this.getContext().getResources().getDisplayMetrics().heightPixels;
        return (height == 0) ? 0 : getY() / (float) height;
    }

    public void setYFraction(float yFraction) {
        int height = this.getContext().getResources().getDisplayMetrics().heightPixels;
        setY((height > 0) ? (yFraction * height) : 0);
    }

    private OnBack onBackListener;
    public void setOnBackListner(OnBack onBackListner){
        this.onBackListener = onBackListner;
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            if(onBackListener != null) {
                onBackListener.onBack();
                return true;
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }
}
