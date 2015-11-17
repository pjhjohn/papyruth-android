package com.papyruth.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.papyruth.android.R;
import com.papyruth.android.fragment.splash.SplashFragment;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        this.getFragmentManager().beginTransaction()
            .add(R.id.fragment_container, new SplashFragment())
            .commit();
    }

    public void startAuthActivity() {
        Intent mainIntent = new Intent(SplashActivity.this, AuthActivity.class);
        SplashActivity.this.startActivity(mainIntent);
        SplashActivity.this.finish();
        SplashActivity.this.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_static_alpha_1);
    }
}
