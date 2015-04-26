package com.montserrat.parts.rating;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.montserrat.activity.MainActivity;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.parts.auth.SignUpStep2Fragment;
import com.montserrat.parts.auth.UserInfo;
import com.montserrat.utils.request.ClientFragment;
import com.montserrat.utils.validator.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by pjhjohn on 2015-04-26.
 * Searches Lecture for Rating on Step 1.
 */
public class RatingStep1Fragment extends ClientFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = super.onCreateView(inflater, container, args);

        /* Bind Views */

        return view;
    }

    public void attemptToNextPage() {

    }

    public void onResponse(JSONObject response) {
        super.onResponse(response);
    }

    public View validate() {
        List<View> vFailed = new ArrayList<View>();
        View candidate;
        // TODO : pick candidates failed to validate certain validation rule.
        return vFailed.isEmpty() ? null : vFailed.get(0);
    }

    public static Fragment newInstance() {
        Fragment fragment = new RatingStep1Fragment();
        Bundle bundle = new Bundle();
        /* For AutoComplete TextView for lecture title & professor name */
        bundle.putString(AppConst.Request.URL, "mont.izz.kr:3000");
        bundle.putString(AppConst.Request.CONTROLLER, "lectures");
        bundle.putString(AppConst.Request.ACTION, "autocomplete");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_rating_step1);
        fragment.setArguments(bundle);
        return fragment;
    }
}