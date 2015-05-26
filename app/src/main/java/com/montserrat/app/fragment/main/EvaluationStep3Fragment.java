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

import com.github.clans.fab.FloatingActionButton;
import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.EvaluationForm;
import com.montserrat.app.model.User;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.support.rx.RxValidator;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;

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

import static com.montserrat.utils.support.rx.RxValidator.toString;
import static com.montserrat.utils.support.rx.RxValidator.isValidEvaluationBody;

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

    @InjectView(R.id.lecture) protected Button lecture;
    @InjectView(R.id.professor) protected Button professor;
    @InjectView(R.id.point_overall) protected RatingBar pointOverall;
    @InjectView(R.id.point_gpa_satisfaction) protected SeekBar pointGpaSatisfaction;
    @InjectView(R.id.point_easiness) protected SeekBar pointEasiness;
    @InjectView(R.id.point_clarity) protected SeekBar pointClarity;
    @InjectView(R.id.body) protected EditText body;
    @InjectView(R.id.fab_done) protected FloatingActionButton submit;
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
    public void onPageFocused () {
        lecture.setText(EvaluationForm.getInstance().getLectureName());
        professor.setText(EvaluationForm.getInstance().getProfessorName());
        pointOverall.setRating(EvaluationForm.getInstance().getPointOverall());
        pointGpaSatisfaction.setProgress(EvaluationForm.getInstance().getPointGpaSatisfaction());
        pointEasiness.setProgress(EvaluationForm.getInstance().getPointEasiness());
        pointClarity.setProgress(EvaluationForm.getInstance().getPointClarity());

        this.subscriptions.add(Observable
            .merge(ViewObservable.clicks(this.lecture), ViewObservable.clicks(this.professor))
            .subscribe(unused -> this.pagerController.setCurrentPage(AppConst.ViewPager.Evaluation.EVALUATION_STEP1, true))
        );

        this.subscriptions.add(WidgetObservable
            .text(body)
            .map(toString)
            .map(isValidEvaluationBody)
            .startWith(false)
            .subscribe(valid -> {
                boolean visible = this.submit.getVisibility() == View.VISIBLE;
                if (visible && !valid) this.submit.hide(true);
                else if (!visible && valid) this.submit.show(true);
            })
        );

        this.subscriptions.add(ViewObservable
            .clicks(submit)
            .map(pass -> {
                EvaluationForm.getInstance().setComment(this.body.getText().toString());
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
    }
}
