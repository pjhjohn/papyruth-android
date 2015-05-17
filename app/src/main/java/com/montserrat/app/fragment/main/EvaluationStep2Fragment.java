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

    @InjectView(R.id.lecture) protected EditText lecture;
    @InjectView(R.id.professor) protected EditText professor;
    @InjectView(R.id.point_overall) protected RatingBar pointOverall;
    @InjectView(R.id.point_gpa_satisfaction) protected SeekBar pointGpaSatisfaction;
    @InjectView(R.id.point_easiness) protected SeekBar pointEasiness;
    @InjectView(R.id.point_clarity) protected SeekBar pointClarity;
    @InjectView(R.id.btn_next) protected Button next;
    private CompositeSubscription subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step2, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();

        /* View Initialization */
        lecture.setEnabled(false);
        professor.setEnabled(false);
        pointOverall.setStepSize((float) 1);
        pointOverall.setMax(10);
        pointGpaSatisfaction.setMax(10);
        pointEasiness.setMax(10);
        pointClarity.setMax(10);

        /* Event binding */
        next.setOnClickListener(v -> {
            EvaluationForm.getInstance().setPointOverall((int) pointOverall.getRating());
            EvaluationForm.getInstance().setPointGpaSatisfaction(pointGpaSatisfaction.getProgress());
            EvaluationForm.getInstance().setPointEasiness(pointEasiness.getProgress());
            EvaluationForm.getInstance().setPointClarity(pointClarity.getProgress());
            this.pagerController.setCurrentPage(AppConst.ViewPager.Evaluation.EVALUATION_STEP3, true);
        });

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
        lecture.setText(EvaluationForm.getInstance().getLectureName());
        professor.setText(EvaluationForm.getInstance().getProfessorName());
    }
}
