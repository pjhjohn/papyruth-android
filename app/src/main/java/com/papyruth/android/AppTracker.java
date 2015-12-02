package com.papyruth.android;

import com.google.android.gms.analytics.Tracker;

public class AppTracker {
    private static AppTracker instance = null;
    private AppTracker() {}
    public static synchronized AppTracker getInstance() {
        if(AppTracker.instance == null) AppTracker.instance = new AppTracker();
        return AppTracker.instance;
    }

    private Tracker mTracker;
    public Tracker getTracker() {
        return mTracker;
    }
    public AppTracker setTracker(Tracker tracker) {
        mTracker = tracker;
        return this;
    }
}
