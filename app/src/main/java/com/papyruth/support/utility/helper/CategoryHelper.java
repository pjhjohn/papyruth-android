package com.papyruth.support.utility.helper;

import android.content.Context;
import android.graphics.PorterDuff;
import android.widget.TextView;

import com.papyruth.android.R;

/**
 * Created by pjhjohn on 2015-12-10.
 */
public class CategoryHelper {
    public static final int MAJOR = 0;
    public static final int LIBERAL_ARTS = 1;

    public static void assignColor(Context context, TextView category, TextView professor, Integer value) {
        int color = context.getResources().getColor(R.color.lecture_type_etc);
        String text = context.getString(R.string.lecture_type_etc);
        if(value != null) {
            switch (value) {
                case MAJOR :
                    color = context.getResources().getColor(R.color.lecture_type_major);
                    text = context.getString(R.string.lecture_type_major);
                    break;
                case LIBERAL_ARTS :
                    color = context.getResources().getColor(R.color.lecture_type_liberal_arts);
                    text = context.getString(R.string.lecture_type_liberal_arts);
                    break;
            }
        }
        category.setText(text);
        category.setTextColor(color);
        category.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        professor.setTextColor(color);
    }
}
