package com.papyruth.support.utility.viewpager;

import android.app.Fragment;

/**
 * Created by pjhjohn on 2015-06-22.
 */
public interface IFragmentFactory {
    Fragment create(int position);
}