package com.papyruth.support.utility.customview;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * SourceCode Originated from "https://github.com/rogcg/gridview-autoresized-images-sample"
 */
public class SquareImageView extends AppCompatImageView {
    public SquareImageView(Context context){
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); //Snap to width
    }
}
