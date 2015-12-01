package com.papyruth.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.R;
import com.papyruth.android.fragment.splash.SplashFragment;
import com.papyruth.android.papyruth;
import com.papyruth.support.utility.error.ErrorHandlerCallback;

import timber.log.Timber;

public class SplashActivity extends Activity implements ErrorHandlerCallback {

    private Tracker mTracker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mTracker = ((papyruth) getApplication()).getTracker();
        this.getFragmentManager().beginTransaction()
            .add(R.id.fragment_container, new SplashFragment())
            .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void startActivity(Class<? extends Activity> targetActivityClass){
        Intent intent = new Intent(SplashActivity.this, targetActivityClass);
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.finish();
    }

    @Override
    public void sendErrorTracker(String cause, String from, boolean isFatal) {
        Timber.d("cause : %s, from : %s", cause, from);
        mTracker.send(new HitBuilders.ExceptionBuilder()
            .setDescription(cause)
            .setFatal(isFatal)
            .build()
        );
    }
}
