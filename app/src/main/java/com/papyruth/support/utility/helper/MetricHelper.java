package com.papyruth.support.utility.helper;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by pjhjohn on 2015-06-20.
 */
public class MetricHelper {
    public static int getPixels(Context context, int resource) {
        int length = 0;
        TypedValue value = new TypedValue();
        if(context.getTheme().resolveAttribute(resource, value, true)) length = TypedValue.complexToDimensionPixelSize(value.data, context.getResources().getDisplayMetrics());
        return length;
    }
    public static int getDisplyWidth(Context context){
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getDisplyHeight(Context context){
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int toPixels(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }
    public static float toPixels(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return dp * density;
    }
    public static int toDP(Context context, int px){
        float density = context.getResources().getDisplayMetrics().density;
        return (int)( px / density);
    }
    public static float toDP(Context context, float px){
        float density = context.getResources().getDisplayMetrics().density;
        return px / density;
    }
}
