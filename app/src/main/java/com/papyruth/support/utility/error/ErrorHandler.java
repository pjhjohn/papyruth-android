package com.papyruth.support.utility.error;

import retrofit.RetrofitError;

public class ErrorHandler {
    public static ErrorHandleResult handle(Throwable throwable, Object object) {
        return handle(throwable, object, false);
    }
    public static ErrorHandleResult handle(Throwable throwable, Object object, boolean toast) {
        if(throwable instanceof RetrofitError) {
            RetrofitError retrofitThrowable = (RetrofitError) throwable;
            switch (retrofitThrowable.getKind()) {
                case HTTP:
                    switch (retrofitThrowable.getResponse().getStatus()) {
                        case 401: return Error401.handle(retrofitThrowable, object, toast);
                        case 403: return Error403.handle(retrofitThrowable, object, toast);
                        case 500: return Error500.handle(retrofitThrowable, object, toast);
                        case 503: return Error503.handle(retrofitThrowable, object, toast);
                        default : return ErrorDefaultHTTP.handle(retrofitThrowable, object, toast);
                    }
                case NETWORK: return ErrorNetwork.handle(retrofitThrowable, object, toast);
                default     : return ErrorDefaultRetrofit.handle(retrofitThrowable, object, toast);
            }
        } else return ErrorDefault.handle(throwable, object, toast);
    }
}
