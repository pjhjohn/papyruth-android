package com.montserrat.controller;

/**
 * Created by pjhjohn on 2015-04-16.
 */
public class AppConst {
    public static class Resource {
        /* For ClientFragment */
        public static final int DEFAULT = 0;
        public static final String PROGRESS = "view_progress";
        public static final String CONTENT = "view_content";
        public static final String FRAGMENT = "view_fragment";

        /* For ClientFragment with ListView */
        public static final String LIST = "view_list";

        /* For ClientFragment with RecyclerView */
        public static final String TOOLBAR = "view_toolbar";
        public static final String FAB = "view_fab";
        public static final String SWIPE_REFRESH = "view_swipe_refresh";
        public static final String RECYCLER = "view_recycler";
    }
    public static class Request {
        /* For ClientFragment - Volley */
        public static final String DEFAULT = "";
        public static final String URL = "url";
        public static final String CONTROLLER = "controller";
        public static final String ACTION = "action";
    }
}
