package com.montserrat.parts.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.activity.R;
import com.montserrat.utils.request.ClientFragmentWithGridView;

import org.json.JSONObject;

public class ProfessorGalleryFragment extends ClientFragmentWithGridView<ProfessorGridItemView> {
    public ProfessorGalleryFragment () {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        /* Set ItemClick Listener for Action */
//        this.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
//            }
//        });

        /* Request for Data */
        this.submit(new JSONObject());

        return view;
    }

    /* TODO : FILL IT. It's necessary. */
    @Override
    protected int getFragmentLayoutId () {
        return R.layout.fragment_main;
    }

    @Override
    protected String getEndpoint () {
        return "http://pjhjohn.appspot.com/search";
    }

//    @Override
//    protected int getListViewId () {
//        return R.id.main_listview;
//    }

    /* TODO : Fill if necessary */
    @Override
    protected int getProgressViewId() {
        return 0;
    }

    @Override
    protected int getContentViewId() {
        return 0;
    }

    @Override
    public void onResponse(JSONObject response) {
        super.onResponse(response);
    }
}