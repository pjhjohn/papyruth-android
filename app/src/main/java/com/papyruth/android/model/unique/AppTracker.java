package com.papyruth.android.model.unique;

import android.app.Application;

import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.papyruth;

public class AppTracker {
    private static AppTracker instance = null;

    public static synchronized AppTracker getInstance() {
        if(AppTracker.instance == null) AppTracker.instance = new AppTracker();
        return AppTracker.instance;
    }

    private Tracker tracker;
    private Application app;

    public Tracker getTracker() {
        return ((papyruth) app).getTracker();
    }

    public AppTracker setTracker(Tracker tracker) {
        this.tracker = tracker;
        return this;
    }
    public AppTracker setTracker(Application application) {
        this.app = application;
        return this;
    }
}
