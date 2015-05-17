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

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.EvaluationForm;
import com.montserrat.app.model.User;
import com.montserrat.utils.etc.RetrofitApi;
import com.montserrat.utils.viewpager.OnPageFocus;
import com.montserrat.utils.viewpager.ViewPagerController;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.view.ViewObservable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class EvaluationStep3Fragment extends Fragment implements OnPageFocus {
    private ViewPagerController pagerController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    @InjectView(R.id.autotext_lecture) protected EditText vLecture;
    @InjectView(R.id.autotext_professor) protected EditText vProfessor;
    @InjectView(R.id.score_overall) protected RatingBar vScoreOverall;
    @InjectView(R.id.score_satisfaction) protected SeekBar vScoreSatisfaction;
    @InjectView(R.id.score_easiness) protected SeekBar vScoreEasiness;
    @InjectView(R.id.score_lecture_quality) protected SeekBar vScoreLectureQuality;
    @InjectView(R.id.description) protected EditText vDescription;
    @InjectView(R.id.submit) protected Button vSubmit;
    private CompositeSubscription subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step3, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();

        /* View Initialization */
        vLecture.setEnabled(false);
        vProfessor.setEnabled(false);
        vScoreOverall.setEnabled(false);
        vScoreSatisfaction.setEnabled(false);
        vScoreEasiness.setEnabled(false);
        vScoreLectureQuality.setEnabled(false);

        vScoreOverall.setStepSize((float) 1);
        vScoreOverall.setMax(10);
        vScoreSatisfaction.setMax(10);
        vScoreEasiness.setMax(10);
        vScoreLectureQuality.setMax(10);

        /* Event Binding */
        this.subscriptions.add( ViewObservable
            .clicks(vSubmit)
            .flatMap(unused -> {
                EvaluationForm.getInstance().setDescription(this.vDescription.getText().toString());
                Timber.d("-------------%s", EvaluationForm.getInstance().toString());
                return RetrofitApi.getInstance().evaluation(
                        User.getInstance().getAccessToken(),
                        EvaluationForm.getInstance().getCourseId(),
                        EvaluationForm.getInstance().getScoreOverall(),
                        EvaluationForm.getInstance().getScoreSatifaction(),
                        EvaluationForm.getInstance().getScoreEasiness(),
                        EvaluationForm.getInstance().getScoreLectureQuality(),
                        EvaluationForm.getInstance().getDescription().toString()
                );
            })
            .subscribe(
                response -> Timber.d("response : %s", response.toString()),
                Throwable::printStackTrace
            )
        );
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    public void onPageFocused () {
        vLecture.setText(EvaluationForm.getInstance().getLectureTitle());
        vProfessor.setText(EvaluationForm.getInstance().getProfessorName());
        vScoreOverall.setRating(EvaluationForm.getInstance().getScoreOverall());
        vScoreSatisfaction.setProgress(EvaluationForm.getInstance().getScoreSatifaction());
        vScoreEasiness.setProgress(EvaluationForm.getInstance().getScoreEasiness());
        vScoreLectureQuality.setProgress(EvaluationForm.getInstance().getScoreLectureQuality());
    }
}
