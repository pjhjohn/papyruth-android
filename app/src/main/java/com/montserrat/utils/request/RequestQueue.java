package com.montserrat.utils.request;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.Request;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by pjhjohn on 2015-04-12.
 */
/* TODO : Is it okay to be a singleton? */
public class RequestQueue {
    private static RequestQueue instance;
    private com.android.volley.RequestQueue queue;
    private static Context context;

    private RequestQueue (Context context) {
        RequestQueue.context = context;
        queue = getRequestQueue();
    }

    public static synchronized RequestQueue getInstance(Context context) {
        if (instance == null) instance = new RequestQueue(context);
        return instance;
    }

    public com.android.volley.RequestQueue getRequestQueue() {
        /* getApplicationContext is key for keeping you from leaking the Activity|BroadcastReceiver if someone passes one in. */
        if (queue == null) queue = Volley.newRequestQueue(context.getApplicationContext());
        return queue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
