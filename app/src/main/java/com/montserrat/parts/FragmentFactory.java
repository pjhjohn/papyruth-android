package com.montserrat.parts;

import android.app.Fragment;

import com.montserrat.parts.auth.AuthenticationFragment;
import com.montserrat.parts.main.MainFragment;

import org.apache.http.auth.AUTH;

/**
 * Created by pjhjohn on 2015-04-09.
 */
public class FragmentFactory {
    private FragmentFactory () {}
    /* TODO : static final integer VS enum */
    public enum Type {
        MAIN, AUTH
    }

    public static Fragment create(Type type, int position) {
        Fragment fragment = null;
        switch (type) {
            case MAIN :
                fragment = MainFragment.newInstance(position);
                break;
            case AUTH :
                fragment = new AuthenticationFragment();
                break;
        } return fragment;
    }
}
