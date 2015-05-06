package com.montserrat.parts.main;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.Request;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.controller.AppManager;
import com.montserrat.parts.auth.User;
import com.montserrat.utils.request.Api;
import com.montserrat.utils.request.ClientFragment;
import com.montserrat.utils.request.RxVolley;
import com.montserrat.utils.viewpager.ViewPagerController;

import org.json.JSONException;
import org.json.JSONObject;

import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class EvaluationStep3Fragment extends Fragment {
    private ViewPagerController pagerController;
    private CompositeSubscription subscriptions;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step3, container, false);

        JSONObject params = new JSONObject();
        try {
            params.put("course_id", 0)
                  .put("score_overall", 0)
                  .put("score_satisfaction", 0)
                  .put("score_easiness", 0)
                  .put("score_lecture_quality", 0)
                  .put("description", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        subscriptions.add(ViewObservable
            .clicks(view.findViewById(R.id.submit))
            .flatMap(unused -> RxVolley.createObservable(Api.url("evaluations"), Request.Method.POST, User.getInstance().getAccessToken(), params))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> Timber.d("response : %s", response),
                Throwable::printStackTrace
            )
        );
        return view;
    }
}
