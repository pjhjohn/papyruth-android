package com.montserrat.parts.auth;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.utils.request.ClientFragment;
import com.montserrat.utils.request.ClientFragmentWithListView;
import com.montserrat.utils.request.ClientFragmentWithRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpUnivFragment extends ClientFragmentWithRecyclerView<SchoolRecyclerAdapter, SchoolRecyclerAdapter.Holder.Data> {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        this.submit();

        return view;
    }

    @Override
    protected SchoolRecyclerAdapter getAdapter (List<SchoolRecyclerAdapter.Holder.Data> items) {
        return SchoolRecyclerAdapter.newInstance(this.items);
    }

    @Override
    public void anotherRequestInProgress () {
        Toast.makeText(this.getActivity(), "Multiple Request Attemption", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponse(JSONObject response) {
        super.onResponse(response);
        Log.d("DEBUG", "response data : " + response);
        try {
            if(response.getBoolean("success")) {
                JSONArray data = response.getJSONArray("data");
                for(int i = 0; i < data.length(); i ++) {
                    JSONObject row = data.getJSONObject(i);
                    this.items.add(new SchoolRecyclerAdapter.Holder.Data(
                            row.getString("name")
                    ));
                }
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
        this.adapter.notifyDataSetChanged();
    }

    public static Fragment newInstance() {
        Fragment fragment = new SignUpUnivFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConst.Request.URL, "pjhjohn.appspot.com");
        bundle.putString(AppConst.Request.CONTROLLER, "university");
        bundle.putString(AppConst.Request.ACTION, "all");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_signup_univ);
        bundle.putInt(AppConst.Resource.RECYCLER, R.id.signup_univ_recyclerview);
        fragment.setArguments(bundle);
        return fragment;
    }
}