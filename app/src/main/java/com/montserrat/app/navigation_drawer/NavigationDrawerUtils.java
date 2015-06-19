package com.montserrat.app.navigation_drawer;

import android.app.Fragment;

import com.montserrat.app.fragment.DummyFragment;
import com.montserrat.app.fragment.main.EvaluationStep1Fragment;
import com.montserrat.app.fragment.main.HomeFragment;
import com.montserrat.app.fragment.main.PartialCourseFragment;
import com.montserrat.app.fragment.main.ProfileFragment;
import com.montserrat.app.fragment.main.SignOutFragment;

/**
 * Created by pjhjohn on 2015-06-16.
 */
public class NavigationDrawerUtils {
    public static class ItemType {
        public static final int HOME            = 0;
        public static final int SEARCH          = 1;
        public static final int RECOMMENDATION  = 2;
        public static final int BOOKMARK        = 3;
        public static final int EVALUATION      = 4;
        public static final int RANDOM          = 5;
    }
    public int getNavigationDrawerPositionOf(Class<? extends Fragment> fragment) {
        switch(fragment.getSimpleName()) {
            default : return 0; // TODO : implement
        }
    }
    public static Class<? extends Fragment> getFragmentClassOf(int position) {
        switch(position) {
            case ItemType.HOME          : return HomeFragment.class;
            case ItemType.SEARCH        : return PartialCourseFragment.class;
            case ItemType.RECOMMENDATION: return DummyFragment.class;
            case ItemType.BOOKMARK      : return DummyFragment.class;
            case ItemType.EVALUATION    : return EvaluationStep1Fragment.class;
            case ItemType.RANDOM        : return DummyFragment.class;
            default: return null;
        }
    }
}
