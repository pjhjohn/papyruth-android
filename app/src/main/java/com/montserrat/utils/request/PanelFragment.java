package com.montserrat.utils.request;

import android.app.Activity;
import android.app.Fragment;
import android.support.v7.widget.Toolbar;

import com.melnykov.fab.FloatingActionButton;
import com.montserrat.app.fragment.nav.NavFragment;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public abstract class PanelFragment extends Fragment {
    private NavFragment.OnCategoryClickListener navigationCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.navigationCallback = (NavFragment.OnCategoryClickListener) activity;
        } catch (ClassCastException e) {
            this.navigationCallback = null;
        }
    }

    protected void setupFloatingActionButton (FloatingActionButton fab) {
        if (fab != null) fab.setOnClickListener( unused -> {
            if(this.navigationCallback != null) this.navigationCallback.onCategorySelected(NavFragment.Category.EVALUATION);
        });
    }
}
