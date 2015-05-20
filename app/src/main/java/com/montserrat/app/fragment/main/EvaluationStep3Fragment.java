package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.montserrat.app.R;
import com.montserrat.app.model.EvaluationForm;
import com.montserrat.app.model.User;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnFocusChange;
import retrofit.RetrofitError;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class EvaluationStep3Fragment extends Fragment implements OnPageFocus, View.OnFocusChangeListener{
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
    @InjectView(R.id.comment) protected EditText comment;
    @InjectView(R.id.submit) protected Button submit;
    private CompositeSubscription subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step3, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();

        /* View Initialization */
        lecture.setEnabled(false);
        professor.setEnabled(false);
        pointOverall.setEnabled(false);
        pointGpaSatisfaction.setEnabled(false);
        pointEasiness.setEnabled(false);
        pointClarity.setEnabled(false);

        pointOverall.setStepSize((float) 1);
        pointOverall.setMax(10);
        pointGpaSatisfaction.setMax(10);
        pointEasiness.setMax(10);
        pointClarity.setMax(10);

        comment.setOnFocusChangeListener(this);
        /* Event Binding */
        this.subscriptions.add(ViewObservable
            .clicks(submit)
            .map(pass -> {
                EvaluationForm.getInstance().setComment(this.comment.getText().toString());
                return pass;
            })
            .observeOn(Schedulers.io())
            .flatMap(click -> RetrofitApi.getInstance().evaluation(
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
        pointOverall.setRating(EvaluationForm.getInstance().getPointOverall());
        pointGpaSatisfaction.setProgress(EvaluationForm.getInstance().getPointGpaSatisfaction());
        pointEasiness.setProgress(EvaluationForm.getInstance().getPointEasiness());
        pointClarity.setProgress(EvaluationForm.getInstance().getPointClarity());
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
//        if(v.getId() == comment.getId())
//            Timber.d("focused??? %s", hasFocus);
        if(!hasFocus) {
            InputMethodManager imm =  (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            Timber.d("v : %s, comment : %s", v.getId(), comment.getId());
        }
    }
}
