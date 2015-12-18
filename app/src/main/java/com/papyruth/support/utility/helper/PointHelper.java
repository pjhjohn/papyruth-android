package com.papyruth.support.utility.helper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.papyruth.android.R;

import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-12-10.
 */
public class PointHelper {
    public static final int MIN =  1;
    public static final int MAX = 10;
    public static final int BOUND_LOWER = 0;
    public static final int BOUND_UPPER = 10;
    public static final int BOUND_UPPER_EXTENDED = 100;
    public static final String NOT_ASSIGNED = "N/A";

    /* Rating Values in float */
    private static boolean pointInRange(float point) {
        return BOUND_LOWER <= point && point <= BOUND_UPPER;
    }
    private static int getPointColor(float value, int from, int to, int colorNone, String[] colorScheme) {
        if(!pointInRange(value)) return colorNone;
        for(int i = 0; i < colorScheme.length; i ++) {
            if(value < from + (to - from) * (float)(i+1)/(float)colorScheme.length) return Color.parseColor(colorScheme[i]);
        } return Color.parseColor(colorScheme[colorScheme.length-1]);
    }
    private static String getPointString(float value) {
        if(!pointInRange(value)) return NOT_ASSIGNED;
        return value >= MAX ? String.valueOf(MAX) : String.format("%.1f", value);
    }

    public static void applyRating(Context context, TextView label, RatingBar ratingbar, TextView point, float value) {
        final int pointColor = getPointColor(value, MIN, MAX, context.getResources().getColor(R.color.point_none), context.getResources().getStringArray(R.array.point_colors));
        label.setTextColor(pointColor);
        point.setTextColor(pointColor);
        point.setText(getPointString(value));
        for(int i = 0; i < 3; i ++) ((LayerDrawable) ratingbar.getProgressDrawable()).getDrawable(i).setColorFilter(pointColor, PorterDuff.Mode.SRC_ATOP);
        ratingbar.setRating(pointInRange(value) ? value / (MAX / ratingbar.getNumStars()) : ratingbar.getNumStars());
    }
    public static void applyRating(Context context, TextView label, RatingBar ratingbar, TextView point, Integer total, Integer count) {
        if(total != null && count != null && count != 0) PointHelper.applyRating(context, label, ratingbar, point, (float) total / (float) count);
        else PointHelper.applyRating(context, label, ratingbar, point, -1);
    }

    /* Rating Values in Integer */
    private static boolean pointInRange(Integer point) {
        return point!=null && BOUND_LOWER <= point && point <= BOUND_UPPER;
    }
    private static int getPointColor(Integer value, int from, int to, int colorNone, String[] colorScheme) {
        if(!pointInRange(value)) return colorNone;
        for(int i = 0; i < colorScheme.length; i ++) {
            if((float)value < from + (to - from) * (float)(i+1)/(float)colorScheme.length) {
                return Color.parseColor(colorScheme[i]);
            }
        } return Color.parseColor(colorScheme[colorScheme.length-1]);
    }
    private static String getPointString(Integer value) {
        Timber.d("value : %d", value);
        if(!pointInRange(value)) return NOT_ASSIGNED;
        return value >= MAX ? String.valueOf(MAX) : String.format("%d.0", value);
    }

    public static void applyRating(Context context, RatingBar ratingbar, Integer value) {
        final int pointColor = getPointColor(value, MIN, MAX, context.getResources().getColor(R.color.point_none), context.getResources().getStringArray(R.array.point_colors));
        Timber.d("Value : %d, Color : %d", value, pointColor);
        for(int i = 0; i < 3; i ++) ((LayerDrawable) ratingbar.getProgressDrawable()).getDrawable(i).setColorFilter(pointColor, PorterDuff.Mode.SRC_ATOP);
        ratingbar.setRating(pointInRange(value) ? (float)value / (MAX / ratingbar.getNumStars()) : ratingbar.getNumStars());
    }
    public static void applyRating(Context context, TextView label, RatingBar ratingbar, TextView point, Integer value) {
        final int pointColor = getPointColor(value, MIN, MAX, context.getResources().getColor(R.color.point_none), context.getResources().getStringArray(R.array.point_colors));
        label.setTextColor(pointColor);
        point.setTextColor(pointColor);
        point.setText(getPointString(value));
        for(int i = 0; i < 3; i ++) ((LayerDrawable) ratingbar.getProgressDrawable()).getDrawable(i).setColorFilter(pointColor, PorterDuff.Mode.SRC_ATOP);
        ratingbar.setRating(pointInRange(value) ? (float)value / (MAX / ratingbar.getNumStars()) : ratingbar.getNumStars());
    }

    /* Progress Values */
    private static boolean pointInRangeExtended(Integer point) {
        return point!=null && BOUND_LOWER <= point && point <= BOUND_UPPER_EXTENDED;
    }
    public static void applyProgress(Context context, TextView prefix, ProgressBar progress, TextView text, Integer total, Integer count) {
        Integer progressValue = count == null || count <= 0 ? null : total == null ? null : (int)(((float) total / (float)count) * 10);
        if(pointInRangeExtended(progressValue)) {
            progress.setProgress(progressValue);
            text.setText(progressValue >= BOUND_UPPER_EXTENDED? "10" : String.format("%d.%d", progressValue / 10, progressValue % 10));
        } else {
            final int colorPointNone = context.getResources().getColor(R.color.point_none);
            prefix.setTextColor(colorPointNone);
            progress.setProgressDrawable(new ColorDrawable(colorPointNone));
            progress.setProgress(100);
            text.setTextColor(colorPointNone);
            text.setText(NOT_ASSIGNED);
        }
    }

    public static void applyProgress(Context context, TextView label, TextView point, TextView postfix, Integer value) {
        if(pointInRange(value)) point.setText(String.valueOf(value));
        else {
            final int colorPointNone = context.getResources().getColor(R.color.point_none);
            label.setTextColor(colorPointNone);
            point.setText(NOT_ASSIGNED);
            point.setTextColor(colorPointNone);
            postfix.setTextColor(colorPointNone);
        }
    }
}
