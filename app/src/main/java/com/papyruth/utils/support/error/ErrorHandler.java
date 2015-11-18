package com.papyruth.utils.support.error;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;

import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.unique.User;

import retrofit.RetrofitError;
import timber.log.Timber;

public class ErrorHandler {
    private static ErrorHandlerCallback callback;
    public static void setApiErrorCallback(ErrorHandlerCallback callback){
        ErrorHandler.callback = callback;
    }

    public static boolean throwError(Throwable error, Object object){
        try{
            if(error instanceof RetrofitError) {
                switch (((RetrofitError) error).getKind()) {
                    case HTTP:
                        switch (((RetrofitError) error).getResponse().getStatus()) {
                            case 403:
                                error403(error, object);
                                break;
                            default:
                                errorHttp(error, object);
                        }
                        break;

                    case NETWORK:
                        errorNetwork(error, object);
                    default:
                        errorDefaultRetrofit(error, object);
                        break;
                }
            }else{
                errorDefault(error, object);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    private static void error403(Throwable error, Object object){
        if(object instanceof Fragment && ((Fragment) object).getActivity() instanceof MainActivity)
            activityChange((Fragment) object, AuthActivity.class);
    }
    private static void errorHttp(Throwable error, Object object){
        if(callback != null)
        callback.sendTracker(((RetrofitError) error).getUrl(), object.getClass().getSimpleName(), false);
        error.printStackTrace();
    }
    private static void errorNetwork(Throwable error, Object object){
        Timber.d("error check : %s\n%s\n%s", error.getMessage(), ((RetrofitError) error).getUrl(), error.getCause());
        if(callback != null)
            callback.sendTracker(error.getMessage(), object.getClass().getSimpleName(), false);
        error.printStackTrace();
    }
    private static void errorDefaultRetrofit(Throwable error, Object object){
        Timber.d("error check : %s\n%s\n%s", error.getMessage(), ((RetrofitError) error).getUrl(), error.getCause());
        if(callback != null)
            callback.sendTracker(error.getMessage(), object.getClass().getSimpleName(), false);
        error.printStackTrace();
    }
    private static void errorDefault(Throwable error, Object object){
        if(callback != null)
            callback.sendTracker(error.getMessage(), object.getClass().getSimpleName(), false);
        error.printStackTrace();
    }

    private String setErrorDescription(int status, String message, String url){
        return String.format("error status : <%d>, msg : <%s>, request url : <%s>", status, message, url);
    }
    private String setErrorDescription(String message, String url){
        return String.format("error msg : <%s>, request url : <%s>", message, url);
    }
    private String setErrorDescription(String message){
        return String.format("error msg : <%s>", message);
    }

    private static void activityChange(Fragment fragment, Class<? extends Activity> targetPath) {
        AppManager.getInstance().clear(AppConst.Preference.HISTORY);
        AppManager.getInstance().remove(AppConst.Preference.ACCESS_TOKEN);
        User.getInstance().clear();
        fragment.getActivity().startActivity(new Intent(fragment.getActivity(), targetPath));
        fragment.getActivity().finish();
    }

}
