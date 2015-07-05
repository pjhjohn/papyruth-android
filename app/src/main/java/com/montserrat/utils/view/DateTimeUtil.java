package com.montserrat.utils.view;

import com.montserrat.app.AppConst;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-06-27.
 */
public class DateTimeUtil {
    public static String convert(String in) {
        return DateTimeUtil.convert(in, AppConst.DateFormat.API, AppConst.DateFormat.SIMPLE);
    }
    public static String convert(String in, String in_format, String out_format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(in_format);
        Date date = null;
        try {
            date = dateFormat.parse(in);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dateFormat.applyPattern(out_format);
        return dateFormat.format(date);
    }
}
