package com.montserrat.parts.auth;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.montserrat.activity.MainActivity;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.controller.AppManager;
import com.montserrat.utils.request.ClientFragment;
import com.montserrat.utils.validator.Validator;
import com.montserrat.utils.viewpager.ViewPagerController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pjhjohn on 2015-04-12.
 */

// TODO : TIMER for minimum loading period
public class LoadingFragment extends ClientFragment {
    private ViewPagerController pagerController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    private TextView vUnivText, vUserText, vEvalText;
    private ImageView vUnivIcon, vUserIcon, vEvalIcon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        /* Bind Views */
        vUnivText = (TextView) view.findViewById(R.id.loading_university_text);
        vUserText = (TextView) view.findViewById(R.id.loading_users_text);
        vEvalText = (TextView) view.findViewById(R.id.loading_evaluations_text);
        vUnivIcon = (ImageView) view.findViewById(R.id.loading_university_image);
        vUserIcon = (ImageView) view.findViewById(R.id.loading_users_image);
        vEvalIcon = (ImageView) view.findViewById(R.id.loading_evaluations_image);

        /* Set Listeners */
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        String access_token = AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null);
        if (access_token == null) {
            // TODO : no access-token -> retrieve data from '/info' -> wait for it -> to AuthFragment
        } else {
            // TODO : has access-token -> retrieve data from '/universities/<university_id> -> wait for it -> to HomeFragment
        }
    }

    @Override
    public void onResponse(JSONObject response) {
        super.onResponse(response);
        AppManager.getInstance().getBoolean(AppConst.Preference.AUTO_SIGNIN, false);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        if(error.networkResponse.statusCode == 401) {
            // TODO : access-token has expired -> retrieve data from '/info' -> wait for it -> to AuthFragment
        }
    }

    public static Fragment newInstance() {
        Fragment fragment = new LoadingFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConst.Request.API_ROOT_URL, AppConst.API_ROOT);
        bundle.putString(AppConst.Request.API_VERSION, AppConst.API_VERSION);
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_loading); // TODO : make a layout and assign id
        fragment.setArguments(bundle);
        return fragment;
    }
}


