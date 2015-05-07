package com.montserrat.utils.request;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.melnykov.fab.FloatingActionButton;
import com.montserrat.app.R;
import com.montserrat.app.AppConst;
import com.montserrat.app.fragment.nav.NavFragment;

import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public abstract class ClientFragmentWithPanels extends ClientFragment {
    protected Toolbar vToolbar;
    protected FloatingActionButton vFAB;
    private int idToolbar, idFAB;
    protected boolean isPending;
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
        if (this.vToolbar == null) Timber.d("Couldn't find Toolbar by ID#%d", this.idToolbar);
        this.vFAB = (FloatingActionButton) view.findViewById(this.idFAB);
        if (this.vFAB == null) Timber.d("Couldn't find FloatingActionButton by ID#%d", this.idFAB);

        if (this.vFAB != null) this.vFAB.setOnClickListener(v -> {
            if(this.navigationCallback != null) this.navigationCallback.onCategorySelected(NavFragment.Category.EVALUATION);
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

    public void onPendingRequest () {
        Toast.makeText(this.getActivity(), this.getResources().getString(R.string.pending), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponse(JSONObject response) {
        super.onResponse(response);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
    }
}
