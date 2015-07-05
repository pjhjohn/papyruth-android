package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.montserrat.app.R;
import com.montserrat.app.model.unique.EvaluationForm;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.navigator.Navigator;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.montserrat.utils.support.rx.RxValidator.isValidEvaluationBody;
import static com.montserrat.utils.support.rx.RxValidator.toString;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class EvaluationStep3Fragment extends Fragment {
    private Navigator navigator;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.navigator = (Navigator) activity;
    }

    @InjectView(R.id.lecture) protected Button lecture;
    @InjectView(R.id.professor) protected Button professor;
    @InjectView(R.id.point_overall) protected RatingBar pointOverall;
    @InjectView(R.id.point_gpa_satisfaction) protected SeekBar pointGpaSatisfaction;
    @InjectView(R.id.point_easiness) protected SeekBar pointEasiness;
    @InjectView(R.id.point_clarity) protected SeekBar pointClarity;
    @InjectView(R.id.evaluation_body) protected EditText body;
    private CompositeSubscription subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step3, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        FloatingActionControl.getInstance().setControl(R.layout.fab_done);

        lecture.setText(EvaluationForm.getInstance().getLectureName());
        professor.setText(EvaluationForm.getInstance().getProfessorName());
        pointOverall.setRating(EvaluationForm.getInstance().getPointOverall());
        pointGpaSatisfaction.setProgress(EvaluationForm.getInstance().getPointGpaSatisfaction());
        pointEasiness.setProgress(EvaluationForm.getInstance().getPointEasiness());
        pointClarity.setProgress(EvaluationForm.getInstance().getPointClarity());

        this.subscriptions.add(Observable
            .merge(ViewObservable.clicks(this.lecture), ViewObservable.clicks(this.professor))
            .subscribe(unused -> this.navigator.navigate(EvaluationStep1Fragment.class, true))
        );

        this.subscriptions.add(WidgetObservable
            .text(this.body)
            .map(toString)
            .map(isValidEvaluationBody)
            .startWith(isValidEvaluationBody.call(this.body.getText().toString()))
            .delay(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe(valid -> {
                boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                if (visible && !valid) FloatingActionControl.getInstance().hide(true);
                else if (!visible && valid) FloatingActionControl.getInstance().show(true);
            })
        );

        this.subscriptions.add(FloatingActionControl
            .clicks()
            .map(pass -> {
                EvaluationForm.getInstance().setComment(this.body.getText().toString());
                return pass;
            })
            .observeOn(Schedulers.io())
            .flatMap(click -> RetrofitApi.getInstance().post_evaluation(
                User.getInstance().getAccessToken(),
                EvaluationForm.getInstance().getCourseId(),
                EvaluationForm.getInstance().getPointOverall(),
                EvaluationForm.getInstance().getPointGpaSatisfaction(),
                EvaluationForm.getInstance().getPointEasiness(),
                EvaluationForm.getInstance().getPointClarity(),
                EvaluationForm.getInstance().getComment()
            ))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    if (response.success)
                        Toast.makeText(this.getActivity(), this.getResources().getString(R.string.submit_evaluation_success), Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(this.getActivity(), this.getResources().getString(R.string.submit_evaluation_fail), Toast.LENGTH_LONG).show();
                },
                error -> {
                    if (error instanceof RetrofitError) {
                        switch (((RetrofitError) error).getResponse().getStatus()) {
                            default:
                                Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                        }
                    }
                }
            )
        );
    }
}
