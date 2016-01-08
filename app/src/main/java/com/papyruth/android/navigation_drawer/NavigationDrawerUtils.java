package com.papyruth.android.navigation_drawer;

import android.app.Fragment;

import com.papyruth.android.fragment.main.FavoriteFragment;
import com.papyruth.android.fragment.main.EvaluationStep1Fragment;
import com.papyruth.android.fragment.main.HomeFragment;
import com.papyruth.android.fragment.main.MyCommentFragment;
import com.papyruth.android.fragment.main.MyEvaluationFragment;

/**
 * Created by pjhjohn on 2015-06-16.
 */
public class NavigationDrawerUtils {
    public static class ItemType {
        public static final int HOME            = 0;
        public static final int EVALUATION      = 1;
        public static final int BOOKMARK        = 2;
        public static final int MY_EVALUATION   = 3;
        public static final int MY_COMMENT      = 4;
    }

    public static Class<? extends Fragment> getFragmentClassOf(int position) {
        switch(position) {
            case ItemType.HOME          : return HomeFragment.class;
            case ItemType.EVALUATION    : return EvaluationStep1Fragment.class;
            case ItemType.BOOKMARK      : return FavoriteFragment.class;
            case ItemType.MY_EVALUATION : return MyEvaluationFragment.class;
            case ItemType.MY_COMMENT    : return MyCommentFragment.class;
            default: return null;
        }
    }
}
