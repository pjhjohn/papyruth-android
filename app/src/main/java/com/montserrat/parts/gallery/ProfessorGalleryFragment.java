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

        return view;
    }
}