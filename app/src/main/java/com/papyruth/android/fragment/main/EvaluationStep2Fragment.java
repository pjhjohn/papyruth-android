package com.papyruth.android.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.PapyruthApplication;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.rx.RxValidator;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.navigator.Navigator;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-26.
 */

public class EvaluationStep2Fragment extends Fragment {
    private Navigator mNavigator;
    private Context mContext;
    private Tracker mTracker;
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
        mTracker = ((PapyruthApplication) getActivity().getApplication()).getTracker();
    }

    @InjectView(R.id.evaluation_form_lecture)                   protected TextView mLecture;
    @InjectView(R.id.evaluation_form_professor)                 protected TextView mProfessor;
    @InjectView(R.id.evaluation_form_overall_ratingbar)         protected RatingBar mRatingBarOverall;
    @InjectView(R.id.evaluation_form_overall_point)             protected TextView mPointOverall;
    @InjectView(R.id.evaluation_form_clarity_seekbar)           protected SeekBar mSeekBarClarity;
    @InjectView(R.id.evaluation_form_clarity_point)             protected TextView mPointClarity;
    @InjectView(R.id.evaluation_form_easiness_seekbar)          protected SeekBar mSeekBarEasiness;
    @InjectView(R.id.evaluation_form_easiness_point)            protected TextView mPointEasiness;
    @InjectView(R.id.evaluation_form_gpa_satisfaction_seekbar)  protected SeekBar mSeekBarGpaSatisfaction;
    @InjectView(R.id.evaluation_form_gpa_satisfaction_point)    protected TextView mPointGpaSatisfaction;
    private CompositeSubscription mCompositeSubscription;
    private Toolbar mToolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step2, container, false);
        ButterKnife.inject(this, view);
        mCompositeSubscription = new CompositeSubscription();
        mToolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);

        mLecture.setText(EvaluationForm.getInstance().getLectureName());
        mProfessor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", getResources().getString(R.string.professor_prefix), EvaluationForm.getInstance().getProfessorName(), " " + getResources().getString(R.string.professor_postfix))));
        LayerDrawable ldRatingBarOverall = (LayerDrawable) mRatingBarOverall.getProgressDrawable();
        for(int i = 0; i < 3; i ++) ldRatingBarOverall.getDrawable(i).setColorFilter(mContext.getResources().getColor(R.color.point_overall), PorterDuff.Mode.SRC_ATOP);
        if(EvaluationForm.getInstance().isNextStep()) {
            mRatingBarOverall.setProgress(EvaluationForm.getInstance().getPointOverall());
            mSeekBarClarity.setProgress(EvaluationForm.getInstance().getPointClarity());
            mSeekBarGpaSatisfaction.setProgress(EvaluationForm.getInstance().getPointGpaSatisfaction());
            mSeekBarEasiness.setProgress(EvaluationForm.getInstance().getPointEasiness());
            mPointOverall.setText(String.valueOf(EvaluationForm.getInstance().getPointOverall()));
            mPointClarity.setText(String.valueOf(EvaluationForm.getInstance().getPointClarity()));
            mPointGpaSatisfaction.setText(String.valueOf(EvaluationForm.getInstance().getPointGpaSatisfaction()));
            mPointEasiness.setText(String.valueOf(EvaluationForm.getInstance().getPointEasiness()));
            FloatingActionControl.getInstance().show(true);
        } else {
            mPointOverall.setText("0");
            mPointClarity.setText("0");
            mPointGpaSatisfaction.setText("0");
            mPointEasiness.setText("0");
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(mCompositeSubscription == null || this.mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_main_write_evaluation2));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mToolbar.setTitle(R.string.toolbar_title_new_evaluation);
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_green).start();
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_green);
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_next);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, false);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, false);
        FloatingActionControl.clicks().observeOn(AndroidSchedulers.mainThread()).subscribe(
            unused -> mNavigator.navigate(EvaluationStep3Fragment.class, true),
            error -> ErrorHandler.handle(error, this)
        );


        mCompositeSubscription.add(Observable
            .combineLatest(
                RxValidator.createObservableRatingBar(mRatingBarOverall, true)
                    .map(rating -> RxValidator.assignRatingValue.call(mPointOverall, rating))
                    .map(rating -> {
                        EvaluationForm.getInstance().setPointOverall((int) (rating * 2));
                        return RxValidator.isFloatValueInRange.call(rating);
                    }),
                RxValidator.createObservableSeekBar(mSeekBarGpaSatisfaction, true)
                    .map(progress -> RxValidator.assignProgressValue.call(mPointGpaSatisfaction, progress))
                    .map(progress -> {
                        EvaluationForm.getInstance().setPointGpaSatisfaction(progress);
                        return RxValidator.isIntegerValueInRange.call(progress);
                    }),
                RxValidator.createObservableSeekBar(mSeekBarEasiness, true)
                    .map(progress -> RxValidator.assignProgressValue.call(mPointEasiness, progress))
                    .map(progress -> {
                        EvaluationForm.getInstance().setPointEasiness(progress);
                        return RxValidator.isIntegerValueInRange.call(progress);
                    }),
                RxValidator.createObservableSeekBar(mSeekBarClarity, true)
                    .map(progress -> RxValidator.assignProgressValue.call(mPointClarity, progress))
                    .map(progress -> {
                        EvaluationForm.getInstance().setPointClarity(progress);
                        return RxValidator.isIntegerValueInRange.call(progress);
                    }),
                (a, b, c, d) -> EvaluationForm.getInstance().isNextStep()
            )
            .startWith(EvaluationForm.getInstance().isNextStep())
            .delay(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe(valid -> {
                boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                if (visible && !valid) FloatingActionControl.getInstance().hide(true);
                else if (!visible && valid) FloatingActionControl.getInstance().show(true);
            }, error -> ErrorHandler.handle(error, this))
        );
    }
}