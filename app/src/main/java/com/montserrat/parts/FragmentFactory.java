package com.montserrat.parts;

import android.app.Fragment;
import android.util.Log;

import com.montserrat.parts.auth.AuthFragment;
import com.montserrat.parts.main.MainFragment;

/**
 * Created by pjhjohn on 2015-04-09.
 */
public class FragmentFactory {
    private FragmentFactory () {}

    /* TODO : static final integer VS enum */
    public enum Type {
        MAIN, AUTH
    }

    public static Fragment create(Type type) {
        switch (type) {
            case MAIN :
                return MainFragment.newInstance();
            case AUTH :
                return AuthFragment.newInstance();
            default :
                throw new RuntimeException("No Fragment found in type " + type);
        }
    }
}
