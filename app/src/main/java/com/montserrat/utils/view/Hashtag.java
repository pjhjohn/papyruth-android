package com.montserrat.utils.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import timber.log.Timber;

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
        this.setTextColor(Color.rgb(84, 107, 141));
        this.setPaintFlags(super.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.setPadding(4, 4, 4, 4);
    }
    private void setupAttrs(Context context, AttributeSet attrs) {

    }
}