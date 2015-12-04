package com.papyruth.support.utility.error;

import retrofit.RetrofitError;

public class ErrorHandler {
    public static boolean handle(Throwable throwable, Object object) {
        if(throwable instanceof RetrofitError) {
            RetrofitError retrofitThrowable = (RetrofitError) throwable;
            switch (retrofitThrowable.getKind()) {
                case HTTP:
                    switch (retrofitThrowable.getResponse().getStatus()) {
                        case 403: return Error403.handle(retrofitThrowable, object);
                        default : return ErrorDefaultHTTP.handle(retrofitThrowable, object);
                    }
                case NETWORK: return ErrorNetwork.handle(retrofitThrowable, object);
                default     : return ErrorDefaultRetrofit.handle(retrofitThrowable, object);
            }
        } else return ErrorDefault.handle(throwable, object);
    }

    public static String setErrorDescription(String errorMsg, String requestUrl, int statusCode) {
        return String.format("Status Code : <%d>\nMessage : <%s>\nRequest url : <%s>", statusCode, errorMsg, requestUrl);
    }
    public static String setErrorDescription(String errorMsg, String requestUrl) {
        return String.format("Message : <%s>\nRequest url : <%s>", errorMsg, requestUrl);
    }
    public static String setErrorDescription(String errorMsg) {
        return String.format("Message : <%s>", errorMsg);
    }
}