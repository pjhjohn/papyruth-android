package com.montserrat.utils.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by pjhjohn on 2015-06-25.
 */
public class Hashtag extends TextView {
    public Hashtag(Context context) {
        super(context);
    }
    public Hashtag(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setupAttrs(context, attrs);
    }
    public Hashtag(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setupAttrs(context, attrs);
    }
    private void setupAttrs(Context context, AttributeSet attrs) {
        super.setGravity(Gravity.CENTER_HORIZONTAL);
        super.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final String txtOriginal = this.getText().toString();
        final String txtDraw = "#" + txtOriginal;

        super.setText(txtDraw);
        super.setTextColor(Color.BLUE);
        getPaint().setFakeBoldText(true);
        super.onDraw(canvas);
        super.setText(txtOriginal);
    }
}