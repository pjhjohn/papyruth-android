package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

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

    @InjectView(R.id.lecture) protected TextView lecture;
    @InjectView(R.id.professor) protected TextView professor;
    @InjectView(R.id.evaluation_point_overall_prefix) protected TextView pointOverallPrefix;
    @InjectView(R.id.evaluation_point_overall_text) protected TextView pointOverallText;
    @InjectView(R.id.evaluation_point_overall_star) protected RatingBar pointOverallRating;
    @InjectView(R.id.evaluation_point_clarity_prefix) protected TextView pointClarityPrefix;
    @InjectView(R.id.evaluation_point_clarity_text) protected TextView pointClarityText;
    @InjectView(R.id.evaluation_point_clarity_progress) protected SeekBar pointClarityProgress;
    @InjectView(R.id.evaluation_point_gpa_satisfaction_prefix) protected TextView pointGpaSatisfactionPrefix;
    @InjectView(R.id.evaluation_point_gpa_satisfaction_text) protected TextView pointGpaSatisfactionText;
    @InjectView(R.id.evaluation_point_gpa_satisfaction_progress) protected SeekBar pointGpaSatisfactionProgress;
    @InjectView(R.id.evaluation_point_easiness_prefix) protected TextView pointEasinessPrefix;
    @InjectView(R.id.evaluation_point_easiness_text) protected TextView pointEasinessText;
    @InjectView(R.id.evaluation_point_easiness_progress) protected SeekBar pointEasinessProgress;
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

        this.lecture.setText(EvaluationForm.getInstance().getLectureName());
        this.professor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", getResources().getString(R.string.professor_prefix), EvaluationForm.getInstance().getProfessorName(), " " + getResources().getString(R.string.professor_postfix))));
        this.pointOverallRating.setOnRatingBarChangeListener((ratingbar, rating, fromUser) -> {
            if (!fromUser) return;
            if (rating >= 10) this.pointOverallText.setText("10");
            else if (rating < 0) this.pointOverallText.setText("N/A");
            else this.pointOverallText.setText(String.format("%d.0", (int)(2*rating)));
            EvaluationForm.getInstance().setPointOverall((int)(2*rating));
        });
        this.setupProgressBar(pointClarityProgress, pointClarityText, value -> EvaluationForm.getInstance().setPointClarity(value));
        this.setupProgressBar(pointGpaSatisfactionProgress, pointGpaSatisfactionText, value -> EvaluationForm.getInstance().setPointGpaSatisfaction(value));
        this.setupProgressBar(pointEasinessProgress, pointEasinessText, value -> EvaluationForm.getInstance().setPointEasiness(value));


        this.subscriptions.add(Observable
            .combineLatest(
                RxValidator.createObservableRatingBar(this.pointOverallRating, true).map(RxValidator.isFloatValueInRange),
                RxValidator.createObservableSeekBar(this.pointGpaSatisfactionProgress, true).map(RxValidator.isIntegerValueInRange),
                RxValidator.createObservableSeekBar(this.pointEasinessProgress, true).map(RxValidator.isIntegerValueInRange),
                RxValidator.createObservableSeekBar(this.pointClarityProgress, true).map(RxValidator.isIntegerValueInRange),
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
    }

    private void setupProgressBar(SeekBar seekbar, TextView score, IntToVoid action) {
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Timber.d("changed : " + seekBar +","+ progress +","+ fromUser);
                if(!fromUser) return;
                if(progress >= 10) score.setText("10");
                else if(progress < 0) score.setText("N/A");
                else score.setText(String.format("%d.0", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                action.func(seekBar.getProgress());
            }
        });
    }

    private interface IntToVoid {
        void func(int value);
    }
}
