package com.papyruth.android.fragment.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.rx.RxValidator;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.fragment.TrackerFragment;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.navigator.Navigator;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-26.
 */

public class EvaluationStep2Fragment extends TrackerFragment {
    private Navigator mNavigator;
    private Context mContext;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mNavigator = (Navigator) activity;
        mContext = activity;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mNavigator = null;
        mContext = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Bind(R.id.evaluation_form_lecture)                   protected TextView mLecture;
    @Bind(R.id.evaluation_form_professor)                 protected TextView mProfessor;
    @Bind(R.id.evaluation_form_overall_ratingbar)         protected RatingBar mRatingBarOverall;
    @Bind(R.id.evaluation_form_overall_point)             protected TextView mPointOverall;
    @Bind(R.id.evaluation_form_clarity_seekbar)           protected SeekBar mSeekBarClarity;
    @Bind(R.id.evaluation_form_clarity_point)             protected TextView mPointClarity;
    @Bind(R.id.evaluation_form_easiness_seekbar)          protected SeekBar mSeekBarEasiness;
    @Bind(R.id.evaluation_form_easiness_point)            protected TextView mPointEasiness;
    @Bind(R.id.evaluation_form_gpa_satisfaction_seekbar)  protected SeekBar mSeekBarGpaSatisfaction;
    @Bind(R.id.evaluation_form_gpa_satisfaction_point)    protected TextView mPointGpaSatisfaction;
    private CompositeSubscription mCompositeSubscription;
    private Toolbar mToolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step2, container, false);
        ButterKnife.bind(this, view);
        mCompositeSubscription = new CompositeSubscription();
        mToolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        mLecture.setPaintFlags(mLecture.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLecture.setText(EvaluationForm.getInstance().getLectureName());
        mProfessor.setText(String.format("%s%s%s", getResources().getString(R.string.professor_prefix), EvaluationForm.getInstance().getProfessorName(), " " + getResources().getString(R.string.professor_postfix)));
        LayerDrawable ldRatingBarOverall = (LayerDrawable) mRatingBarOverall.getProgressDrawable();
        for(int i = 0; i < 3; i ++) ldRatingBarOverall.getDrawable(i).setColorFilter(mContext.getResources().getColor(R.color.point_overall), PorterDuff.Mode.SRC_ATOP);
        if(EvaluationForm.getInstance().isNextStep()) {
            mRatingBarOverall.setProgress(EvaluationForm.getInstance().getPointOverall());
            mPointOverall.setText(String.valueOf(EvaluationForm.getInstance().getPointOverall()));
            mSeekBarClarity.setProgress(EvaluationForm.getInstance().getPointClarity());
            mPointClarity.setText(String.valueOf(EvaluationForm.getInstance().getPointClarity()));
            mSeekBarGpaSatisfaction.setProgress(EvaluationForm.getInstance().getPointGpaSatisfaction());
            mPointGpaSatisfaction.setText(String.valueOf(EvaluationForm.getInstance().getPointGpaSatisfaction()));
            mSeekBarEasiness.setProgress(EvaluationForm.getInstance().getPointEasiness());
            mPointEasiness.setText(String.valueOf(EvaluationForm.getInstance().getPointEasiness()));
            FloatingActionControl.getInstance().show(true);
        } else {
            RxValidator.assignRatingValue.call(mPointOverall, mRatingBarOverall.getRating());
            RxValidator.assignProgressValue.call(mPointClarity, mSeekBarClarity.getProgress());
            RxValidator.assignProgressValue.call(mPointEasiness, mSeekBarEasiness.getProgress());
            RxValidator.assignProgressValue.call(mPointGpaSatisfaction, mSeekBarGpaSatisfaction.getProgress());
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        if(mCompositeSubscription == null || this.mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mToolbar.setTitle(R.string.toolbar_title_new_evaluation);
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_green).start();
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_green);
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_next);
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SEARCH, false);
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SETTING, false);
        FloatingActionControl.clicks().observeOn(AndroidSchedulers.mainThread()).subscribe(
            unused -> mNavigator.navigate(EvaluationStep3Fragment.class, true),
            error -> ErrorHandler.handle(error, this)
        );


        mCompositeSubscription.add(Observable
            .combineLatest(
                RxValidator.createObservableRatingBar(mRatingBarOverall, true)
                    .map(overall -> RxValidator.assignRatingValue.call(mPointOverall, overall))
                    .map(overall -> {
                        EvaluationForm.getInstance().setPointOverall((int) (overall * 2));
                        return (int) (overall * 2);
                    }),
                RxValidator.createObservableSeekBar(mSeekBarClarity, true)
                    .map(clarity -> RxValidator.assignProgressValue.call(mPointClarity, clarity))
                    .map(clarity -> {
                        if(clarity >= 0) EvaluationForm.getInstance().setPointClarity(clarity);
                        return clarity;
                    }),
                RxValidator.createObservableSeekBar(mSeekBarEasiness, true)
                    .map(easiness -> RxValidator.assignProgressValue.call(mPointEasiness, easiness))
                    .map(easiness -> {
                        if(easiness >= 0) EvaluationForm.getInstance().setPointEasiness(easiness);
                        return easiness;
                    }),
                RxValidator.createObservableSeekBar(mSeekBarGpaSatisfaction, true)
                    .map(satisfaction -> RxValidator.assignProgressValue.call(mPointGpaSatisfaction, satisfaction))
                    .map(satisfaction -> {
                        if(satisfaction >= 0) EvaluationForm.getInstance().setPointGpaSatisfaction(satisfaction);
                        return satisfaction;
                    }),
                (overall, clarity, easiness, satisfaction) -> {
                    return EvaluationForm.getInstance().isNextStep() && satisfaction == RxValidator.ON_STOP_TRACKING_TOUCH_SEEKBAR; // Show FAB only when GPA Satisfaction control has finished
                }
            )
            .startWith(EvaluationForm.getInstance().isNextStep())
            .subscribe(valid -> {
                boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                if(visible && !valid) FloatingActionControl.getInstance().hide(true);
                else if(!visible && valid) FloatingActionControl.getInstance().show(true);
            }, error -> ErrorHandler.handle(error, this))
        );
    }
}