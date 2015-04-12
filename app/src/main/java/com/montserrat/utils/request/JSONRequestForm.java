package com.montserrat.utils.request;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class JSONRequestForm {
    private static final int DEFAULT_CONNECTION_TIMEOUT_MS = 5000;
    private final OnResponse receiver;
    private final OnRequest requester;
    private final String endpoint;
    private JSONObject json;
    private int timeout;

    public static interface OnRequest {
        void onRequest();                             // 리퀘스트 던질 때 먼저 실행될 것.
    }

    public static interface OnResponse {
        void onSuccess(String responseBody);          // 성공
        void onTimeout(String errorMsg);              // 타임아웃
        void onNoInternetConnection(String errorMsg); // 인터넷 연결 안되어있을때
        void onCanceled();                            // 사용자가 취소했을 때 처리
    }

    public JSONRequestForm(OnRequest requester, OnResponse receiver, String endpoint) {
        /* Essential Requirements for http request */
        this.requester= requester;
        this.receiver = receiver;
        this.endpoint = endpoint;

        /* Default Values */
        this.json = new JSONObject();
        this.timeout  = DEFAULT_CONNECTION_TIMEOUT_MS; /* Default Timeout interval in msfor connection : 5000ms = 5seconds */
    }

    public JSONRequestForm setTimeoutInterval(int timeoutMilliseconds) {
        this.timeout = timeoutMilliseconds;
        return this;
    }
    /* Map building functions of JSONObject to this.json */
    public JSONRequestForm accumulate(String key, Object value) throws JSONException {
        this.json.accumulate(key, value);
        return this;
    }
    public JSONRequestForm put(String key, boolean value) throws JSONException {
        this.json.put(key, value);
        return this;
    }
    public JSONRequestForm put(String key, java.util.Collection value) throws JSONException {
        this.json.put(key, value);
        return this;
    }
    public JSONRequestForm put(String key, double value) throws JSONException {
        this.json.put(key, value);
        return this;
    }
    public JSONRequestForm put(String key, int value) throws JSONException {
        this.json.put(key, value);
        return this;
    }
    public JSONRequestForm put(String key, long value) throws JSONException {
        this.json.put(key, value);
        return this;
    }
    public JSONRequestForm put(String key, java.util.Map value) throws JSONException {
        this.json.put(key, value);
        return this;
    }
    public JSONRequestForm put(String key, Object value) throws JSONException {
        this.json.put(key, value);
        return this;
    }
    public JSONRequestForm putOpt(String key, Object value) throws JSONException {
        this.json.putOpt(key, value);
        return this;
    }
    public void clear() {
        this.json = new JSONObject();
    }

    public void submit() throws JSONException, UnsupportedEncodingException {
        if(this.requester!=null) this.requester.onRequest();
        new JSONRequestAsyncTask(this.receiver, this.endpoint, this.json, this.timeout).submit();
    }
}
