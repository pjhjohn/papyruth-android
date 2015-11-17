package com.papyruth.utils.support.error;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;

import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.model.unique.User;

import retrofit.RetrofitError;

public class ErrorHandler {
    private static ErrorHandlerCallback callback;
    public static void setApiErrorCallback(ErrorHandlerCallback callback){
        ErrorHandler.callback = callback;
    }


    public static boolean throwError(Throwable error, Fragment errorFrgament){
        try{
            if(error instanceof RetrofitError) {
                switch (((RetrofitError) error).getKind()) {
                    case HTTP:
                        switch (((RetrofitError) error).getResponse().getStatus()) {
                            case 403:
                                activityChange(errorFrgament, AuthActivity.class);
                                break;
                            default:
                                callback.sendTracker(((RetrofitError) error).getUrl(), errorFrgament.getClass().getSimpleName());
                        }
                        break;

                    case NETWORK:
                        callback.sendTracker(error.getMessage(), errorFrgament.getClass().getSimpleName());
                        break;

                    default:
                        callback.sendTracker(error.getMessage(), errorFrgament.getClass().getSimpleName());
                        break;
                }
            }else{
                callback.sendTracker(error.getMessage(), errorFrgament.getClass().getSimpleName());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private static void activityChange(Fragment fragment, Class<? extends Activity> targetPath) {
        AppManager.getInstance().clear(AppConst.Preference.HISTORY);
        AppManager.getInstance().remove(AppConst.Preference.ACCESS_TOKEN);
        User.getInstance().clear();
        fragment.getActivity().startActivity(new Intent(fragment.getActivity(), targetPath));
        fragment.getActivity().finish();
    }

}
