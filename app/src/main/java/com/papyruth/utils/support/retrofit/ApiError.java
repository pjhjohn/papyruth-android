package com.papyruth.utils.support.retrofit;

import android.app.Fragment;

import retrofit.RetrofitError;

public class ApiError {
    private static ApiErrorCallback callback;
    public static void setApiErrorCallback(ApiErrorCallback callback){
        ApiError.callback = callback;
    }

    public static boolean throwError(Throwable error, Class<? extends Fragment> errorFrom){
        try{
            if(error instanceof RetrofitError) {
                switch (((RetrofitError) error).getKind()) {
                    case HTTP:
                        switch (((RetrofitError) error).getResponse().getStatus()) {
                            case 403:
                                break;
                            default:
                                callback.sendTracker(((RetrofitError) error).getUrl(), errorFrom.getSimpleName());
                        }
                        break;

                    case NETWORK:
                        callback.sendTracker(error.getMessage(), errorFrom.getSimpleName());
                        break;

                    default:
                        callback.sendTracker(error.getMessage(), errorFrom.getSimpleName());
                        break;
                }
            }else{
                callback.sendTracker(error.getMessage(), errorFrom.getSimpleName());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

}
