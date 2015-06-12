package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.SeekBar;

import com.montserrat.app.R;
import com.montserrat.app.model.unique.EvaluationForm;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.rx.RxValidator;
import com.montserrat.utils.view.navigator.Navigator;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-26.
 */

public class EvaluationStep2Fragment extends Fragment {
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
    private CompositeSubscription subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step2, container, false);
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
        FloatingActionControl.getInstance().setControl(R.layout.fab_next);

        lecture.setText(EvaluationForm.getInstance().getLectureName());
        professor.setText(EvaluationForm.getInstance().getProfessorName());

        this.subscriptions.add(Observable
            .combineLatest(
                RxValidator.createObservableRatingBar(this.pointOverall, true).map(RxValidator.isFloatValueInRange),
                RxValidator.createObservableSeekBar(this.pointGpaSatisfaction, true).map(RxValidator.isIntegerValueInRange),
                RxValidator.createObservableSeekBar(this.pointEasiness, true).map(RxValidator.isIntegerValueInRange),
                RxValidator.createObservableSeekBar(this.pointClarity, true).map(RxValidator.isIntegerValueInRange),
                (Boolean a, Boolean b, Boolean c, Boolean d) -> a && b && c && d
            )
            .startWith(EvaluationStep3Fragment.class.getName().equals(this.navigator.getBackStackNameAt(1)))
            .delay(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe(valid -> {
                boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                if (visible && !valid) FloatingActionControl.getInstance().hide(true);
                else if (!visible && valid) FloatingActionControl.getInstance().show(true);
            }, error -> {
                Timber.d("error : %s", error);
            })
        );

        this.subscriptions.add(Observable
            .merge(ViewObservable.clicks(this.lecture), ViewObservable.clicks(this.professor))
            .subscribe(unused -> this.navigator.navigate(EvaluationStep1Fragment.class, true))
        );

        this.subscriptions.add(FloatingActionControl
            .clicks()
            .subscribe(unused -> {
                EvaluationForm.getInstance().setPointOverall((int) pointOverall.getRating());
                EvaluationForm.getInstance().setPointGpaSatisfaction(pointGpaSatisfaction.getProgress());
                EvaluationForm.getInstance().setPointEasiness(pointEasiness.getProgress());
                EvaluationForm.getInstance().setPointClarity(pointClarity.getProgress());
                this.navigator.navigate(EvaluationStep3Fragment.class, true);
            })
        );
    }
}
