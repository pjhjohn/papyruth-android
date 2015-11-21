package com.papyruth.utils.view;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.papyruth.android.R;

/**
 * Created by pjhjohn on 2015-06-25.
 */
public class Hashtag extends TextView {
    public Hashtag(Context context, String value) {
        super(context);
        this.setup(context);
        this.setText("#"+value);
    }

    private void setup(Context context) {
        this.setGravity(Gravity.CENTER_HORIZONTAL);
        this.setTextColor(context.getResources().getColor(R.color.hashtag));
        this.setPaintFlags(super.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.setPadding(4, 4, 4, 4);
    }
    private void setupAttrs(Context context, AttributeSet attrs) {

    }
}