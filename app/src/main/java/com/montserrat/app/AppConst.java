package com.montserrat.app;

/**
 * Created by pjhjohn on 2015-04-16.
 */
public class AppConst {
    /* API BINDING */
    public static final String API_ROOT = "mont.izz.kr:3001";
    public static final String API_VERSION = "v1";

    public static final int MIN_ADMISSION_YEAR = 2000;
    /* KOREAN : 1 WORD == 3 BYTES */
    public static final int MAX_REALNAME_BYTES = 20;
    public static final int MAX_NICKNAME_BYTES = 20;
    public static final int DEFAULT_RECYCLERVIEW_THRESHOLD_TO_ASK_MORE = 5;
    public static final int MIN_EVALUATION_BODY_BYTES = 30;

    /**
     * Hierachy of ViewPager is directly mapped to FragmentFactory & ViewPagerManager
     * Subclass of ViewPager should be registered as Type of FragmentFactory
     */
    public static class ViewPager {
        /* For Auth Activity */
        public static class Auth {
            public static final int LENGTH = 4;
            public static final int LOADING = 0;
            public static final int AUTH = 1;
            public static final int SIGNUP_STEP1 = 2;
            public static final int SIGNUP_STEP2 = 3;
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
    }
}
