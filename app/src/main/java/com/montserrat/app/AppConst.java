package com.montserrat.app;

import android.graphics.Color;

/**
 * Created by pjhjohn on 2015-04-16.
 */
public class AppConst {
    /* API BINDING */
    public static final String API_ROOT = "mont.izz.kr:3001";
    public static final String API_VERSION = "v1";

    public static final int MIN_ENTRANCE_YEAR = 2000;
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
        public static final String SIMPLE = "yyyy.MM.dd";
    }

    public static final int COLOR_POSITIVE = Color.rgb(201, 57, 64);
    public static final int COLOR_NEGATIVE = Color.rgb(36, 80, 141);
    public static final int COLOR_NEUTRAL = Color.rgb(100, 100, 100);

    public static final int COLOR_GRAY = Color.rgb(230, 230, 230);
    public static final int COLOR_GRAY_ACCENT = Color.rgb(242, 242, 242);
    public static final int COLOR_HIGHLIGHT_YELLOW = Color.rgb(255, 194, 0);

    public static final int COLOR_POINT_OVERALL = Color.rgb(0, 0, 0);
    public static final int COLOR_POINT_CLARITY = Color.rgb(65, 183, 174);
    public static final int COLOR_POINT_GPA_SATISFACTION = Color.rgb(84, 107, 141);
    public static final int COLOR_POINT_EASINESS = Color.rgb(224, 94, 95);

    public static final int COLOR_POINT_LOW = Color.rgb(65, 183, 174);
    public static final int COLOR_POINT_HIGH = Color.rgb(224, 94, 95);


    /**
     * Hierachy of ViewPager is directly mapped to FragmentFactory & ViewPagerManager
     * Subclass of ViewPager should be registered as Type of FragmentFactory
     */
    public static class ViewPager {
        /* For Auth Activity */
        public static class Auth {
            public static final int LENGTH = 8;
            public static final int LOADING = 0;
            public static final int AUTH = 1;
            public static final int SIGNUP_UNIV = 2;
            public static final int SIGNUP_STEP1 = 3;
            public static final int SIGNUP_STEP2 = 4;
            public static final int SIGNUP_STEP3 = 5;
            public static final int SIGNUP_STEP4 = 6;
            public static final int SIGNUP_TERM = 7;
        }
    }

    /* Preferences */
    public static class Preference {
        /* Preference Storage Name */
        public static final String STORAGE_NAME = "montserrat_sharedpref";

        /* Keys */
        public static final String AUTO_SIGNIN = "auto_signin";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String HISTORY = "history";
        public static final String SEARCH = "search";

        /* Validate */
        public static final String EMAIL = "email";
        public static final String NICKNAME = "nickname";

    }
    public static class Tag{
        public static final String ACTIVE_FRAGMENT = "FRAGMENT";
    }
}
