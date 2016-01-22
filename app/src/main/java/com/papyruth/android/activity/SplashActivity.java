package com.papyruth.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.BuildConfig;
import com.papyruth.android.PapyruthApplication;
import com.papyruth.android.R;
import com.papyruth.android.fragment.splash.SplashFragment;
import com.papyruth.support.utility.error.Error;

import timber.log.Timber;

public class SplashActivity extends Activity implements Error.OnReportToGoogleAnalytics {

    private Tracker mTracker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mTracker = ((PapyruthApplication) getApplication()).getTracker();
        Crashlytics.setBool(getResources().getString(R.string.crashlytics_key_debug_mode), BuildConfig.DEBUG);
        this.getFragmentManager().beginTransaction()
            .add(R.id.fragment_container, new SplashFragment())
            .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onBackPressed() {
        this.finish();
        System.exit(0);
    }

    public void startActivity(Class<? extends Activity> targetActivityClass){
        Intent intent = new Intent(SplashActivity.this, targetActivityClass);
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.finish();
    }

    @Override
    public void onReportToGoogleAnalytics(String description, String source, boolean fatal) {
        Timber.d("SplashActivity.onReportToGoogleAnalytics from %s\nCause : %s", source, description);
        mTracker.send(new HitBuilders.ExceptionBuilder().setDescription(description).setFatal(fatal).build());
    }
}
