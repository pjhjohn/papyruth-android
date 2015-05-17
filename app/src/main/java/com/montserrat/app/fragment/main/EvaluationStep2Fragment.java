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
import com.montserrat.utils.viewpager.OnPageFocus;
import com.montserrat.utils.viewpager.ViewPagerController;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-26.
 */

public class EvaluationStep2Fragment extends Fragment implements OnPageFocus {
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
    @InjectView(R.id.btn_next) protected Button vNext;
    private CompositeSubscription subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step2, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();

        /* View Initialization */
        vLecture.setEnabled(false);
        vProfessor.setEnabled(false);
        vScoreOverall.setStepSize((float) 1);
        vScoreOverall.setMax(10);
        vScoreSatisfaction.setMax(10);
        vScoreEasiness.setMax(10);
        vScoreLectureQuality.setMax(10);

        /* Event binding */
        vNext.setOnClickListener(v -> next());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }


    public void next(){
        EvaluationForm.getInstance().setScoreOverall((int) vScoreOverall.getRating());
        EvaluationForm.getInstance().setScoreSatifaction(vScoreSatisfaction.getProgress());
        EvaluationForm.getInstance().setScoreEasiness(vScoreEasiness.getProgress());
        EvaluationForm.getInstance().setScoreLectureQuality(vScoreLectureQuality.getProgress());
        this.pagerController.setCurrentPage(AppConst.ViewPager.Evaluation.EVALUATION_STEP3, true);
    }

    @Override
    public void onPageFocused () {
        vLecture.setText(EvaluationForm.getInstance().getLectureTitle());
        vProfessor.setText(EvaluationForm.getInstance().getProfessorName());
    }
}
