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

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.RequestFuture;
import com.google.android.gms.internal.io;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
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
    private TextView vUnivText, vUserText, vEvalText;
    private ImageView vUnivIcon, vUserIcon, vEvalIcon;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loading, container, false);
        this.vUnivText = (TextView) view.findViewById(R.id.loading_university_text);
        this.vUserText = (TextView) view.findViewById(R.id.loading_users_text);
        this.vEvalText = (TextView) view.findViewById(R.id.loading_evaluations_text);
        (this.vUnivIcon = (ImageView) view.findViewById(R.id.loading_university_image)).setImageResource(R.drawable.snu_logo);
        (this.vUserIcon = (ImageView) view.findViewById(R.id.loading_users_image)).setImageResource(R.drawable.snu_logo);
        (this.vEvalIcon = (ImageView) view.findViewById(R.id.loading_evaluations_image)).setImageResource(R.drawable.snu_logo);
        return view;
    }

    private Subscription requestSubscription;
    private Subscription timerSubscription;
    private boolean requestReady, timerReady, proceedToMainActivity;
    @Override
    public void onResume() {
        super.onResume();
        requestReady = false;
        timerReady = false;
        proceedToMainActivity = false;
    }
    @Override
    public void onStart() {
        super.onStart();
        /* Step 1 : Get access-token -> Request user information */
        UserInfo userinfo = UserInfo.getInstance();
        if(userinfo.getAccessToken() == null || userinfo.getAccessToken().toString().isEmpty()) userinfo.setAccessToken(AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null));
        requestSubscription = observableJsonObjectRequest( "http://mont.izz.kr:3001/api/v1/users/me", (String)userinfo.getAccessToken(), new JSONObject())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    response -> { /* Success with valid token -> Fill up UserInfo and proceed */
                        requestSubscription.unsubscribe();
                        Log.e("DEBUG", response.toString());
                        UserInfo.getInstance().setData(response.optJSONObject("user"));
                        Log.e("DEBUG", "userinfo : " + UserInfo.getInstance());
                        this.fetchUniversityStatistics();
                    },
                    error -> { /* Token no longer valid -> To the AuthFragment */
                        if (error instanceof VolleyError) {
                            int code = ((VolleyError) error).networkResponse.statusCode;
                            if (code == 401) {
                                this.requestReady = true;
                                this.fetchGlobalStatistics();
                            }
                        } else error.printStackTrace();
                    }
            );
        /* Set Timer for 5 seconds */
        timerSubscription = Observable.just(true)
                .delay(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    Log.d("DEBUG", "Timer Terminated");
                    this.timerReady = true;
                    this.finish();
                });
    }

    /* Unsubscribe */
    @Override
    public void onStop() {
        super.onStop();
        if(requestSubscription!= null && !requestSubscription.isUnsubscribed()) requestSubscription.unsubscribe();
        if(timerSubscription!= null && !timerSubscription.isUnsubscribed()) timerSubscription.unsubscribe();
    }

    private void fetchUniversityStatistics () {
        if(!requestSubscription.isUnsubscribed()) requestSubscription.unsubscribe();
        requestSubscription = observableJsonObjectRequest( "http://mont.izz.kr:3001/api/v1/universities/" + UserInfo.getInstance().getUniversityId(), (String)UserInfo.getInstance().getAccessToken(), new JSONObject())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> { /* Success with valid token -> render view & proceed to MainActivity */
                            Log.d("DEBUG", response.toString());
                            JSONObject data = response.optJSONObject("university");
                            this.vUnivText.setText(""+data.optInt("university_count"));
                            this.vUserText.setText(""+data.optInt("user_count"));
                            this.vEvalText.setText(""+data.optInt("evaluation_count"));
                            this.requestSubscription.unsubscribe();
                            this.requestReady = true;
                            this.proceedToMainActivity = true;
                            this.finish();
                        },
                        Throwable::printStackTrace
                );
    }

    private void fetchGlobalStatistics() {
        if(!requestSubscription.isUnsubscribed()) requestSubscription.unsubscribe();
        requestSubscription = observableJsonObjectRequest( "http://mont.izz.kr:3001/api/v1/info", new JSONObject())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            Log.d("DEBUG", response.toString());
                            this.vUnivText.setText(response.optInt("university_count"));
                            this.vUserText.setText(response.optInt("user_count"));
                            this.vEvalText.setText(response.optInt("evaluation_count"));
                            this.requestSubscription.unsubscribe();
                            this.requestReady = true;
                            this.finish();
                        },
                        Throwable::printStackTrace
                );
    }

    private void finish() {
        Log.d("DEBUG", String.format("<timerReady:%b> <requestReady:%b> <proceedToMainActivity:%b>", timerReady, requestReady, proceedToMainActivity));
        if (timerReady && requestReady) {
            if (proceedToMainActivity) {
                this.getActivity().startActivity(new Intent(this.getActivity(), MainActivity.class));
                this.getActivity().finish();
            } else {
                this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.AUTH, false);
            }
        }
    }

    /* Observable */
    public Observable<JSONObject> observableJsonObjectRequest(String url, JSONObject body) {
        return observableJsonObjectRequest(url, Request.Method.GET, null, body);
    }
    public Observable<JSONObject> observableJsonObjectRequest(String url, String token, JSONObject body) {
        return observableJsonObjectRequest(url, Request.Method.GET, token, body);
    }
    public Observable<JSONObject> observableJsonObjectRequest(String url, int method, String token, JSONObject body) {
        return Observable.create(subscriber -> {
            RequestQueue.getInstance(this.getActivity()).addToRequestQueue(new JsonObjectRequest(
                    method,
                    url,
                    body,
                    (response) -> {
                        Log.d("DEBUG", response.toString());
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(response);
                            subscriber.onCompleted();
                        }
                    },
                    (error) -> {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onError(error);
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    if (token != null) headers.put("Authorization", String.format("Token token=\"%s\"", token));
                    Log.d("DEBUG", String.format("<url:%s> <token:%s>\n request body : %s", url, token, body.toString()));
                    return headers;
                }
            });
        });
    }

    public static Fragment newInstance() {
        return new LoadingFragment();
    }
}


