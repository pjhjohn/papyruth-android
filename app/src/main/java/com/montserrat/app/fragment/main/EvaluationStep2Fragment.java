package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.SeekBar;

import com.montserrat.app.R;
import com.montserrat.app.AppConst;
import com.montserrat.utils.viewpager.ViewPagerController;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-26.
 */

public class EvaluationStep2Fragment extends Fragment {
    private ViewPagerController pagerController;

    @InjectView(R.id.autotext_lecture) protected EditText vLecture;
    @InjectView(R.id.autotext_professor) protected EditText vProfessor;
    @InjectView(R.id.score_overall) protected RatingBar vScoreOverall;
    @InjectView(R.id.score_satisfaction) protected SeekBar vScoreSatisfaction;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step2, container, false);

        view.findViewById(R.id.btn_next).setOnClickListener(v -> next());
        ButterKnife.inject(this, view);
        vLecture.setFocusableInTouchMode(false);
        vProfessor.setFocusableInTouchMode(false);
        vScoreOverall.setStepSize((float)0.5);
        vScoreOverall.setMax(10);
        vScoreSatisfaction.setMax(10);

        getFragmentManager().beginTransaction().add(this, AppConst.Tag.Evaluation.EVALUATION_STEP2);


        return view;
    }
    public void next(){
        this.pagerController.setCurrentPage(AppConst.ViewPager.Evaluation.EVALUATION_STEP3, true);
    }
    public void update(String lecture, String professor){
        vLecture.setText(lecture);
        vProfessor.setText(professor);
    }
}
