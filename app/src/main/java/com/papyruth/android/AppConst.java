package com.papyruth.android;

import android.content.Context;

/**
 * Created by pjhjohn on 2015-04-16.
 */
public class AppConst {
    /* API BINDING */
    public static final String API_BASE_RELEASE = "api.papyruth.com:443";
    public static final String API_BASE_DEBUG = "api.papyruth.com:443";
    public static final String API_VERSION_RELEASE = "v1";
    public static final String API_VERSION_DEBUG = "v1";

    /* Retrofit REST Adapter*/
    public static final String LOGGER_TAG = "RetrofitApi";
    public static final String LOGGER_FILTER = "^[AC\\-\\<\\{].*";

    /* KOREAN : 1 WORD == 3 BYTES */
    public static final int MAX_REALNAME_BYTES = 20;
    public static final int MAX_NICKNAME_BYTES = 20;
    public static final int DEFAULT_RECYCLERVIEW_THRESHOLD_TO_ASK_MORE = 5;
    public static final int MIN_EVALUATION_BODY_BYTES = 30;

    /* in milli-seconds */
    public static final int ANIM_DURATION_SHORT = 200;
    public static final int ANIM_DURATION_MEDIUM = 400;
    public static final int ANIM_DURATION_LONG = 600;

    public static final int ANIM_ACCELERATION = 1;
    public static final int ANIM_DECELERATION = 1;

    /* DateTime String Format*/
    public static class DateFormat {
        public static final String API = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"; // TODO : Z stands for TimeZone, but seems not working now
        public static final String DATE = "yyyy.MM.dd";
        public static final String DATE_TIME_12HR = "yyyy.MM.dd ahh:mm";
    }

    public static class Navigator {
        public static class Auth {
            public static final int LENGTH       = 5;
            public static final int SIGNIN       = 0;
            public static final int SIGNUP_STEP1 = 1;
            public static final int SIGNUP_STEP2 = 2;
            public static final int SIGNUP_STEP3 = 3;
            public static final int SIGNUP_STEP4 = 4;
        }
    }

    public static int[] DEFAULT_PROGRESSBAR_COLOR_SCHEME(Context context) {
        return new int[] {
            context.getResources().getColor(R.color.colorchip_green),
            context.getResources().getColor(R.color.colorchip_blue),
            context.getResources().getColor(R.color.colorchip_red)
        };
    }

    /* Preferences */
    public static class Preference {
        /* Preference Storage Name */
        public static final String STORAGE_NAME = "montserrat_sharedpref";

        /* Keys */
        public static final String ACCESS_TOKEN = "access_token";
        public static final String HISTORY = "history";
    }

    public static class Tag {
        public static final String ACTIVE_FRAGMENT = "FRAGMENT";
    }

    public static class Menu {
        public static final int SEARCH = R.id.menu_search;
        public static final int SETTING = R.id.menu_setting;
    }
}
