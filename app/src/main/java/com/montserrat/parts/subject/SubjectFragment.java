package com.montserrat.parts.subject;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.montserrat.activity.R;
import com.montserrat.utils.adapter.UniversalAdapter;
import com.montserrat.utils.requestable_fragment.JSONRequestableFragmentWithListView;

import java.util.Random;

public class SubjectFragment extends JSONRequestableFragmentWithListView<SubjectListItemView> {
    public SubjectFragment () {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        Random random = new Random();
        Log.d("DEBUG", "" + this.items);
        this.items.clear();
        this.items.add(new SubjectListItemView(new SubjectListItemView.Data(this.getString(R.string.main_dummy_subject1), this.getString(R.string.main_dummy_professor1), random.nextFloat() * 5)));
        this.items.add(new SubjectListItemView(new SubjectListItemView.Data(this.getString(R.string.main_dummy_subject2), this.getString(R.string.main_dummy_professor2), random.nextFloat() * 5)));
        this.items.add(new SubjectListItemView(new SubjectListItemView.Data(this.getString(R.string.main_dummy_subject3), this.getString(R.string.main_dummy_professor3), random.nextFloat() * 5)));
        this.items.add(new SubjectListItemView(new SubjectListItemView.Data(this.getString(R.string.main_dummy_subject4), this.getString(R.string.main_dummy_professor4), random.nextFloat() * 5)));
        this.items.add(new SubjectListItemView(new SubjectListItemView.Data(this.getString(R.string.main_dummy_subject5), this.getString(R.string.main_dummy_professor5), random.nextFloat() * 5)));
        this.items.add(new SubjectListItemView(new SubjectListItemView.Data(this.getString(R.string.main_dummy_subject6), this.getString(R.string.main_dummy_professor6), random.nextFloat() * 5)));
        this.items.add(new SubjectListItemView(new SubjectListItemView.Data(this.getString(R.string.main_dummy_subject7), this.getString(R.string.main_dummy_professor7), random.nextFloat() * 5)));
        this.listview.setAdapter(new UniversalAdapter(
                this.items, this.getActivity().getApplicationContext()
        ));
        Log.d("DEBUG", "" + this.listview);
        this.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
                // Do Something
            }
        });
        return view;
    }

    @Override
    protected int getFragmentLayoutId () {
        return R.layout.main_fragment;
    }
    @Override
    protected int getListViewId () {
        return R.id.main_listview;
    }
    @Override
    protected String getEndpoint () {
        return "http://pjhjohn.appspot.com/main";
    }
    @Override
    public void onSuccess (String responseBody) {

    }
    @Override
    public void onTimeout (String errorMsg) {

    }
    @Override
    public void onNoInternetConnection (String errorMsg) {

    }
    @Override
    public void onCanceled () {

    }

    public static Fragment newInstance (int i) {
        return new SubjectFragment();
    }
}