package com.montserrat.utils.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.montserrat.app.AppManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-05-02.
 * Support Class for wrapping Volley with RxAndroid
 */
public class RxVolley {
    public static Observable<JSONObject> createObservable (String url, JSONObject body) {
        return createObservable(url, Request.Method.GET, null, body);
    }
    public static Observable<JSONObject> createObservable (String url, String token, JSONObject body) {
        return createObservable(url, Request.Method.GET, token, body);
    }
    public static Observable<JSONObject> createObservable (String url, int method, JSONObject body) {
        return createObservable(url, method, null, body);
    }
    public static Observable<JSONObject> createObservable (String url, int method, String token, JSONObject body) {
        return Observable
            .create(observer -> RequestQueue.getInstance(AppManager.getInstance().getContext())
                .addToRequestQueue(new JsonObjectRequest(
                    method,
                    url,
                    body,
                    response -> {
                        Timber.d("Response : %s", response);
                        if (!observer.isUnsubscribed()) {
                            try {
                                observer.onNext(response.put("status", 200));
                                observer.onCompleted();
                            } catch (JSONException e) {
                                Timber.e(e, "JSONException @ RxVolley");
                                observer.onError(e);
                            }
                        }
                    },
                    error -> {
                        if (!observer.isUnsubscribed()) {
                            try {
                                Timber.d(error, new String(((VolleyError)error).networkResponse.data));
                                observer.onNext(new JSONObject().put("status", error.networkResponse.statusCode).put("message", error.toString()));
                                observer.onCompleted();
                            } catch (JSONException e) {
                                Timber.e(e, "JSONException @ RxVolley");
                                observer.onError(e);
                            }
                        }
                    }
                ) {
                    @Override
                    public Map<String, String> getHeaders () throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        if (token != null) headers.put("Authorization", token);
                        return headers;
                    }
                }));
    }
}
