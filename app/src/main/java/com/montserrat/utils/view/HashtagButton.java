package com.montserrat.utils.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonRectangle;

/**
 * Created by pjhjohn on 2015-06-25.
 */
public class HashtagButton extends ButtonRectangle {
    public HashtagButton(Context context, String value) {
        super(context, null);
        this.setup(context);
        this.setText("#"+value);
    }

    private void setup(Context context) {
        this.setGravity(Gravity.CENTER_HORIZONTAL);
        this.setTextColor(Color.rgb(84, 107, 141));
        this.setPadding(4, 4, 4, 4);
    }
    private void setupAttrs(Context context, AttributeSet attrs) {

    }
}