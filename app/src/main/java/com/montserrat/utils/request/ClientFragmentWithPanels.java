package com.montserrat.utils.request;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.android.volley.VolleyError;
import com.melnykov.fab.FloatingActionButton;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.parts.navigation_drawer.NavFragment;
import com.montserrat.utils.recycler.PanelControllerOnScrollWithAskMore;
import com.montserrat.utils.recycler.RecyclerViewAskMoreListener;
import com.montserrat.utils.recycler.RecyclerViewClickListener;
import com.montserrat.utils.viewpager.ViewPagerController;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public abstract class ClientFragmentWithPanels extends ClientFragment {
    private static final String TAG = "ClientFragment";
    private Toolbar vToolbar;
    protected FloatingActionButton vFAB;
    private int idToolbar, idFAB;
    private boolean isPending;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        /* Bind Parameters */
        if ( this.args != null ) {
            this.idToolbar = this.args.getInt(AppConst.Resource.TOOLBAR, AppConst.Resource.DEFAULT);
            this.idFAB = this.args.getInt(AppConst.Resource.FAB, AppConst.Resource.DEFAULT);
        } else {
            this.idToolbar = AppConst.Resource.DEFAULT;
            this.idFAB = AppConst.Resource.DEFAULT;
        }

        /* Initialize other members */
        this.isPending = false;

        /* Bind Views */
        this.vToolbar = (Toolbar) this.getActivity().findViewById(this.idToolbar);
        if (this.vToolbar == null) Log.d(TAG, "Couldn't find Toolbar by ID#" + this.idToolbar);
        this.vFAB = (FloatingActionButton) view.findViewById(this.idFAB);
        if (this.vFAB == null) Log.d(TAG, "Couldn't find FloatingActionButton by ID#" + this.idFAB);

        this.vFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if(ClientFragmentWithPanels.this.navigationCallback != null)
                    ClientFragmentWithPanels.this.navigationCallback.onCategorySelected(NavFragment.Category.RATING);
            }
        });

        return view;
    }

    @Override
    public void submit() {
        super.submit();
        if(this.isPending) {
            this.onPendingRequest(); // handle duplicated reqeust
        }
    }

    public abstract void onPendingRequest ();

    @Override
    public void onResponse(JSONObject response) {
        super.onResponse(response);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
    }
}
