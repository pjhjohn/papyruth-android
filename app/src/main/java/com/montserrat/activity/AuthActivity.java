package com.montserrat.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import com.montserrat.parts.FragmentFactory;
import com.montserrat.parts.auth.AuthFragment;

/**
 * Activity For Authentication.
 */
public class AuthActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        FragmentManager fragmentManager = this.getFragmentManager();
        Fragment fragment = FragmentFactory.create(FragmentFactory.Type.AUTH);
        fragmentManager
                .beginTransaction()
                .add(R.id.activity_signin, fragment)
                .commit();
    }
}