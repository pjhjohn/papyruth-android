package com.papyruth.support.utility.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.PapyruthApplication;

import timber.log.Timber;

/**
 * Created by SSS on 2015-12-09.
 */
public class TrackerFragment extends Fragment {
    protected Tracker mTracker;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mTracker = ((PapyruthApplication) getActivity().getApplication()).getTracker();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(this.getClass().getSimpleName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        Timber.d("GA : %s", this.getClass().getSimpleName());
    }
}
