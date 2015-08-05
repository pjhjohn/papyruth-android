package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.unique.EvaluationForm;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.rx.RxValidator;
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
        this.setRatingBarColor(this.pointOverallRatingBar, Color.YELLOW);
        Picasso.with(this.getActivity()).load(R.drawable.avatar_dummy).into(this.pointOverallIcon);
        Picasso.with(this.getActivity()).load(R.drawable.avatar_dummy).into(this.pointClarityIcon);
        Picasso.with(this.getActivity()).load(R.drawable.avatar_dummy).into(this.pointGpaSatisfactionIcon);
        Picasso.with(this.getActivity()).load(R.drawable.avatar_dummy).into(this.pointEasinessIcon);

        this.subscriptions.add(Observable
            .combineLatest(
                RxValidator.createObservableRatingBar(this.pointOverallRatingBar, true).startWith(0.0f)
                    .map(rating -> RxValidator.assignRatingValue.call(this.pointOverallText, rating))
                    .map(rating -> {
                        EvaluationForm.getInstance().setPointOverall((int) (rating * 2));
                        return RxValidator.isFloatValueInRange.call(rating);
                    }),
                RxValidator.createObservableSeekBar(this.pointGpaSatisfactionSeekBar, true).startWith(0)
                    .map(progress -> RxValidator.assignProgressValue.call(this.pointGpaSatisfactionText, progress))
                    .map(progress -> {
                        EvaluationForm.getInstance().setPointGpaSatisfaction(progress);
                        return RxValidator.isIntegerValueInRange.call(progress);
                    }),
                RxValidator.createObservableSeekBar(this.pointEasinessSeekBar, true).startWith(0)
                    .map(progress -> RxValidator.assignProgressValue.call(this.pointEasinessText, progress))
                    .map(progress -> {
                        EvaluationForm.getInstance().setPointEasiness(progress);
                        return RxValidator.isIntegerValueInRange.call(progress);
                    }),
                RxValidator.createObservableSeekBar(this.pointClaritySeekBar, true).startWith(0)
                    .map(progress -> RxValidator.assignProgressValue.call(this.pointClarityText, progress))
                    .map(progress -> {
                        EvaluationForm.getInstance().setPointClarity(progress);
                        return RxValidator.isIntegerValueInRange.call(progress);
                    }),
                (Boolean a, Boolean b, Boolean c, Boolean d) -> a && b && c && d
            )
            .startWith(EvaluationStep3Fragment.class.getName().equals(this.navigator.getBackStackNameAt(1)))
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