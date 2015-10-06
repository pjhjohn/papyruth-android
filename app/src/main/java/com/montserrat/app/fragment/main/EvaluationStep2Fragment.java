package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.unique.EvaluationForm;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.rx.RxValidator;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.navigator.Navigator;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
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
    @Override
    public void onDetach() {
        super.onDetach();
        this.navigator = null;
    }

    @InjectView(R.id.lecture) protected TextView lecture;
    @InjectView(R.id.professor) protected TextView professor;
    @InjectView(R.id.evaluation_point_overall_icon) protected ImageView pointOverallIcon;
    @InjectView(R.id.evaluation_point_overall_text) protected TextView pointOverallText;
    @InjectView(R.id.evaluation_point_overall_rating) protected RatingBar pointOverallRatingBar;
    @InjectView(R.id.evaluation_point_clarity_icon) protected ImageView pointClarityIcon;
    @InjectView(R.id.evaluation_point_clarity_text) protected TextView pointClarityText;
    @InjectView(R.id.evaluation_point_clarity_seekbar) protected SeekBar pointClaritySeekBar;
    @InjectView(R.id.evaluation_point_gpa_satisfaction_icon) protected ImageView pointGpaSatisfactionIcon;
    @InjectView(R.id.evaluation_point_gpa_satisfaction_text) protected TextView pointGpaSatisfactionText;
    @InjectView(R.id.evaluation_point_gpa_satisfaction_seekbar) protected SeekBar pointGpaSatisfactionSeekBar;
    @InjectView(R.id.evaluation_point_easiness_icon) protected ImageView pointEasinessIcon;
    @InjectView(R.id.evaluation_point_easiness_text) protected TextView pointEasinessText;
    @InjectView(R.id.evaluation_point_easiness_seekbar) protected SeekBar pointEasinessSeekBar;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step2, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.toolbar_title_new_evaluation);
        ToolbarUtil.getColorTransitionAnimator(toolbar, AppConst.COLOR_POINT_EASINESS).start();

        final Context context = this.getActivity();
        this.lecture.setText(EvaluationForm.getInstance().getLectureName());
        this.professor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", getResources().getString(R.string.professor_prefix), EvaluationForm.getInstance().getProfessorName(), " " + getResources().getString(R.string.professor_postfix))));
        this.setRatingBarColor(this.pointOverallRatingBar, Color.YELLOW);
        Picasso.with(context).load(R.drawable.ic_point_overall).into(this.pointOverallIcon);
        Picasso.with(context).load(R.drawable.ic_point_clarity).into(this.pointClarityIcon);
        Picasso.with(context).load(R.drawable.ic_point_satisfaction).into(this.pointGpaSatisfactionIcon);
        Picasso.with(context).load(R.drawable.ic_point_easiness).into(this.pointEasinessIcon);
        if(EvaluationForm.getInstance().isNextStep()){
            this.pointOverallRatingBar.setProgress(EvaluationForm.getInstance().getPointOverall());
            this.pointClaritySeekBar.setProgress(EvaluationForm.getInstance().getPointClarity());
            this.pointGpaSatisfactionSeekBar.setProgress(EvaluationForm.getInstance().getPointGpaSatisfaction());
            this.pointEasinessSeekBar.setProgress(EvaluationForm.getInstance().getPointEasiness());
            this.pointOverallText.setText(EvaluationForm.getInstance().getPointOverall().toString());
            this.pointClarityText.setText(EvaluationForm.getInstance().getPointClarity().toString());
            this.pointGpaSatisfactionText.setText(EvaluationForm.getInstance().getPointGpaSatisfaction().toString());
            this.pointEasinessText.setText(EvaluationForm.getInstance().getPointEasiness().toString());
            FloatingActionControl.getInstance().show(true);
        }else {
            this.pointOverallText.setText("0");
            this.pointClarityText.setText("0");
            this.pointGpaSatisfactionText.setText("0");
            this.pointEasinessText.setText("0");
        }

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
        FloatingActionControl.clicks().observeOn(AndroidSchedulers.mainThread())
            .subscribe(unused -> {
                this.navigator.navigate(EvaluationStep3Fragment.class, true);
            });

        this.subscriptions.add(Observable
            .combineLatest(
                RxValidator.createObservableRatingBar(this.pointOverallRatingBar, true)
                    .map(rating -> RxValidator.assignRatingValue.call(this.pointOverallText, rating))
                    .map(rating -> {
                        EvaluationForm.getInstance().setPointOverall((int) (rating * 2));
                        return RxValidator.isFloatValueInRange.call(rating);
                    }),
                RxValidator.createObservableSeekBar(this.pointGpaSatisfactionSeekBar, true)
                    .map(progress -> RxValidator.assignProgressValue.call(this.pointGpaSatisfactionText, progress))
                    .map(progress -> {
                        EvaluationForm.getInstance().setPointGpaSatisfaction(progress);
                        return RxValidator.isIntegerValueInRange.call(progress);
                    }),
                RxValidator.createObservableSeekBar(this.pointEasinessSeekBar, true)
                    .map(progress -> RxValidator.assignProgressValue.call(this.pointEasinessText, progress))
                    .map(progress -> {
                        EvaluationForm.getInstance().setPointEasiness(progress);
                        return RxValidator.isIntegerValueInRange.call(progress);
                    }),
                RxValidator.createObservableSeekBar(this.pointClaritySeekBar, true)
                    .map(progress -> RxValidator.assignProgressValue.call(this.pointClarityText, progress))
                    .map(progress -> {
                        EvaluationForm.getInstance().setPointClarity(progress);
                        return RxValidator.isIntegerValueInRange.call(progress);
                    }),
                (a, b, c, d) -> {
                    return EvaluationForm.getInstance().isNextStep();
                }
            )
            .startWith(EvaluationForm.getInstance().isNextStep())
            .delay(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe(valid -> {
                boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                if (visible && !valid) FloatingActionControl.getInstance().hide(true);
                else if (!visible && valid) FloatingActionControl.getInstance().show(true);
            }, error -> Timber.d("error : %s", error))
        );
    }

    private void setRatingBarColor(RatingBar rating, int color) {
        LayerDrawable stars = (LayerDrawable) rating.getProgressDrawable();
        for(int i = 0; i < 3; i ++) stars.getDrawable(i).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }
}