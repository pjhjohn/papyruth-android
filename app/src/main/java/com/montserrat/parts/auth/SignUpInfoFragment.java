package com.montserrat.parts.auth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.montserrat.utils.requestable_fragment.JSONRequestableFragment;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpInfoFragment extends JSONRequestableFragment {
    public SignUpInfoFragment (){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    @Override protected String getEndpoint() { return null; }
    @Override protected int getFragmentLayoutId() { return 0; }
    @Override protected int getProgressViewId() { return 0; }
    @Override protected int getContentViewId() { return 0; }

    @Override
    public void onSuccess(String jsonStr) {
        super.onSuccess(jsonStr);
        JSONObject json = null;
        try {
            json = new JSONObject(jsonStr);
        } catch (JSONException e) {
            Log.d(this.getClass().toString(), "ERROR IN PARSING JSON RESPONSE DATA : " + jsonStr);
            e.printStackTrace();
        }
        if (json == null) return;

        /* JSONObject Response Ready*/
        //...
    }
    @Override
    public void onTimeout(String errorMsg) {
        super.onTimeout(errorMsg);
        Toast.makeText(this.getActivity(), "인터넷 연결이 불안정합니다.", Toast.LENGTH_LONG).show();

    }
    @Override
    public void onNoInternetConnection(String errorMsg) {
        super.onNoInternetConnection(errorMsg);
        Toast.makeText(this.getActivity(), "인터넷 연결이 되어있지 않습니다.", Toast.LENGTH_LONG).show();
    }
}
