package com.papyruth.support.opensource.retrofit.apis;

/**
 * Created by pjhjohn on 2015-10-31.
 */
public class Api {
    /* Singleton */
    private static Api instance = null;
    public static Api getInstance() {
        return instance;
    }

    /* Instance Creation */
    private Api(Papyruth papyruthApi) {
        this.papyruthApi = papyruthApi;
    }

    /**
     * Only triggered when first triggered.
     * @param papyruthApi
     */
    public static synchronized void createInstance(Papyruth papyruthApi) {
        if(Api.instance != null) return;
        Api.instance = new Api(papyruthApi);
    }

    /* Register Papyruth Api*/
    private final Papyruth papyruthApi;
    public static Papyruth papyruth() {
        return Api.instance.papyruthApi;
    }
}
