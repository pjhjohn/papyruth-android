package com.montserrat.utils.view;

import android.content.Context;
import android.content.res.Resources;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by pjhjohn on 2015-06-27.
 */
public class DateTimeUtil {
    public static String timestamp(String in) {
        return DateTimeUtil.timestamp(in, AppConst.DateFormat.API, AppConst.DateFormat.SIMPLE);
    }
    public static String timestamp(String in, String in_format, String out_format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(in_format);
        Date date = null;
        try {
            date = dateFormat.parse(in);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dateFormat.applyLocalizedPattern(out_format);
        return dateFormat.format(date);
    }

    public static String timeago(Context context, String in) {
        return DateTimeUtil.timeago(context, in, AppConst.DateFormat.API);
    }

    private static final String NOT_ASSIGNED = "N/A";
    public static String timeago(Context context, String in, String in_format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(in_format);
        Date date = null;
        try {
            date = dateFormat.parse(in);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(date == null) return NOT_ASSIGNED;
        return getTimeAgo(context, Calendar.getInstance().getTimeInMillis(), date.getTime(), timestamp(in));
    }

    private static final long _A_SECOND = 1000;           // One second (in milliseconds)
    private static final long _A_MINUTE = 60 * _A_SECOND; // One minute (in milliseconds)
    private static final long _AN_HOUR  = 60 * _A_MINUTE; // One hour   (in milliseconds)
    private static final long _A_DAY    = 24 * _AN_HOUR;  // One day    (in milliseconds)
    public static String getTimeAgo(Context context, long now, long then, String exactDate) {
        if (then > now || then <= 0) return NOT_ASSIGNED;
        final Resources res = context.getResources();
        final long time_difference = now - then;

        if      (time_difference < _A_MINUTE) return res.getString(R.string.just_now);
        else if (time_difference < _AN_HOUR) return res.getString(R.string.time_ago, res.getQuantityString(R.plurals.minutes, (int) (time_difference / _A_MINUTE), time_difference / _A_MINUTE));
        else if (time_difference < _A_DAY) return res.getString(R.string.time_ago, res.getQuantityString(R.plurals.hours, (int) (time_difference / _AN_HOUR), time_difference / _AN_HOUR));
        else if (time_difference <  2 * _A_DAY) return res.getString(R.string.yesterday);
        else if (time_difference < 30 * _A_DAY) return res.getString(R.string.time_ago, res.getQuantityString(R.plurals.days, (int) (time_difference / _A_DAY), time_difference / _A_DAY));
        else return exactDate;
    }
}
