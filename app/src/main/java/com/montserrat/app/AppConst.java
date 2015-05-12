package com.montserrat.app;

/**
 * Created by pjhjohn on 2015-04-16.
 */
public class AppConst {
    /* API BINDING */
    public static final String API_ROOT = "mont.izz.kr:3001/api";
    public static final String API_VERSION = "v1";

    public static final int MIN_ADMISSION_YEAR = 2000;
    /* KOREAN : 1 WORD == 3 BYTES */
    public static final int MAX_REALNAME_BYTES = 20;
    public static final int MAX_NICKNAME_BYTES = 20;
    public static final int DEFAULT_RECYCLERVIEW_THRESHOLD_TO_ASK_MORE = 5;

    public static class Resource {
        /* For ClientFragment */
        public static final int DEFAULT = 0;
        public static final String PROGRESS = "resource_progress";
        public static final String CONTENT = "resource_content";
        public static final String FRAGMENT = "resource_fragment";

        /* For ClientFragment with ListView */
        public static final String LIST = "resource_list";

        /* For ClientFragment with RecyclerView */
        public static final String TOOLBAR = "resource_toolbar";
        public static final String FAB = "resource_fab";
        public static final String SWIPE_REFRESH = "resource_swipe_refresh";
        public static final String RECYCLER = "resource_recycler";

        /* Initializer for Fragment */
        public static final String INITIALIZER = "resource_initializer";
    }
    public static class Request {
        /* For ClientFragment - Volley */
        public static final String DEFAULT = "";
        public static final String METHOD = "request_method";
        public static final String API_ROOT_URL = "request_api_root_url";
        public static final String API_VERSION = "request_api_vertion";
        public static final String ACTION = "request_action";
        public static class Method {
            public static final int GET = com.android.volley.Request.Method.GET;
            public static final int POST = com.android.volley.Request.Method.POST;
        }
    }

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

        /* For Main Activity : Subclasses are 1 : 1 mapped to navigation category */
        public static class Home {
            public static final int LENGTH = 1;
            public static final int HOME = 0;
        }
        public static class Search {
            public static final int LENGTH = 1;
            public static final int BRIEF = 0;
        }
        public static class Recommendation {
            public static final int LENGTH = 1;
            public static final int DUMMY = 0;
        }
        public static class Evaluation {
            public static final int LENGTH = 3;
            public static final int EVALUATION_STEP1 = 0;
            public static final int EVALUATION_STEP2 = 1;
            public static final int EVALUATION_STEP3 = 2;
        }
        public static class Random {
            public static final int LENGTH = 1;
            public static final int DUMMY = 0;
        }
        public static class Profile {
            public static final int LENGTH = 1;
            public static final int DUMMY = 0;
        }
        public static class Signout {
            public static final int LENGTH = 1;
            public static final int DUMMY = 0;
        }
    }

    /* Preferences */
    public static class Preference {
        /* Preference Storage Name */
        public static final String STORAGE_NAME = "montserrat_sharedpref";

        /* Keys */
        public static final String AUTO_SIGNIN = "auto_signin";
        public static final String ACCESS_TOKEN = "access_token";
    }

    public static class Tag{
        public static class Evaluation{
            public static final String EVALUATION_STEP1 = "evaluation_step1";
            public static final String EVALUATION_STEP2 = "evaluation_step2";
            public static final String EVALUATION_STEP3 = "evaluation_step3";
        }
    }
}
