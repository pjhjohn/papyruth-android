package com.montserrat.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

import com.montserrat.parts.auth.AuthenticationFragment;
import com.montserrat.utils.requestable_fragment.JSONRequestableFragment;

public class AuthActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        FragmentManager fragmentManager = this.getFragmentManager();
        JSONRequestableFragment fragment = new AuthenticationFragment();
        fragmentManager
                .beginTransaction()
                .add(R.id.activity_signin, fragment)
                .commit();
    }
}