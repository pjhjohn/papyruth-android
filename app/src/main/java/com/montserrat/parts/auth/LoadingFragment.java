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
import android.util.Log;
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
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.RequestFuture;
import com.montserrat.activity.MainActivity;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.controller.AppManager;
import com.montserrat.utils.request.ClientFragment;
import com.montserrat.utils.request.RequestQueue;
import com.montserrat.utils.validator.Validator;
import com.montserrat.utils.viewpager.ViewPagerController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Created by pjhjohn on 2015-04-12.
 */

// TODO : TIMER for minimum loading period
public class LoadingFragment extends Fragment {
    /* Setup ViewPagerController */
    private ViewPagerController pagerController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }
    /* Inflate Fragment View */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loading, container, false);
    }

    /*----------------------  RxAndroid  ----------------------*/

    Subscription _subscription;
    /* Subscribe */
    @Override
    public void onResume() {
        super.onResume();
        _subscription = observableJsonObjectRequest(new JSONObject(), Request.Method.GET, "http://mont.izz.kr:3001/api/v1/info").subscribe(JsonObjectRequestObserver());
    }
    /* Unsubscribe */
    @Override
    public void onPause() {
        super.onPause();
        _subscription.unsubscribe();
    }

    /* Observable */
    public Observable<JSONObject> observableJsonObjectRequest(JSONObject body, int method, String url) {
        return Observable.create(subscription -> {
            RequestQueue.getInstance(this.getActivity()).addToRequestQueue(new JsonObjectRequest(
                method,
                url,
                body,
                subscription::onNext,
                subscription::onError
            ){
                @Override
                public Map<String, String> getHeaders () {
                    Map<String, String> params = new HashMap<>();
                    params.put("Authorization", String.format("Token token = %s", AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, "")));
                    return params;
                }
            });
        });
    }

    /* Observer */
    public Observer<JSONObject> JsonObjectRequestObserver() {
        return new Observer<JSONObject>() {
            @Override
            public void onCompleted () {
                Log.e("DEBUG", "onCompleted");
            }

            @Override
            public void onError (Throwable e) {
                Log.e("DEBUG", "onError");
                VolleyError ve = (VolleyError) e;
                if(ve.networkResponse.statusCode == 401) onStatus401(ve);
                else if(ve.networkResponse.statusCode == 403) onStatus403(ve);
            }

            @Override
            public void onNext (JSONObject response) {
                Log.e("DEBUG", "Response : " + response);
            }
        };
    }

    public void onStatus401(VolleyError e) {
        Log.e("DEBUG", "Status 401 : Not Authorized");
    }
    public void onStatus403(VolleyError e ) {
        Log.e("DEBUG", "Status 403 : Forbidden ");
    }

    public static Fragment newInstance() {
        return new LoadingFragment();
    }
}


