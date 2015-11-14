package com.montserrat.app.navigation_drawer;

import android.app.Fragment;

import com.montserrat.app.fragment.main.BookmarkFragment;
import com.montserrat.app.fragment.main.EvaluationStep1Fragment;
import com.montserrat.app.fragment.main.HomeFragment;
import com.montserrat.app.fragment.main.MyCommentFragment;
import com.montserrat.app.fragment.main.MyEvaluationFragment;

/**
 * Created by pjhjohn on 2015-06-16.
 */
public class NavigationDrawerUtils {
    public static class ItemType {
        public static final int HOME            = 0;
        public static final int BOOKMARK        = 1;
        public static final int EVALUATION      = 2;
        public static final int MYEVALUATION    = 3;
        public static final int MYCOMMENT       = 4;
    }
    public int getNavigationDrawerPositionOf(Class<? extends Fragment> fragment) {
        switch(fragment.getSimpleName()) {
            default : return 0; // TODO : implement
        }
    }
    public static Class<? extends Fragment> getFragmentClassOf(int position) {
        switch(position) {
            case ItemType.HOME          : return HomeFragment.class;
            case ItemType.BOOKMARK      : return BookmarkFragment.class;
            case ItemType.EVALUATION    : return EvaluationStep1Fragment.class;
            case ItemType.MYEVALUATION  : return MyEvaluationFragment.class;
            case ItemType.MYCOMMENT     : return MyCommentFragment.class;
            default: return null;
        }
    }
}
