package com.montserrat.app.fragment.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.montserrat.app.R;
import com.montserrat.app.adapter.CourseAdapter;
import com.montserrat.app.model.CourseData;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.model.unique.EvaluationForm;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.viewpager.OnBack;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Course Fragment
 * - HEADER @ 0 position of recycler view
 * - COURSE @ 1 position of recycler view
 * - SIMPLE_EVALUATION @ 2+ position of recycler view : has ability to be expanded when clicked
 */
public class CourseFragment extends RecyclerViewFragment<CourseAdapter, EvaluationData> implements OnBack {
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

    @InjectView(R.id.course_recyclerview) protected RecyclerView courseRecyclerView;
    @InjectView(R.id.evaluation_container) protected FrameLayout evaluationContainer;
    private Toolbar toolbar;
    private Fragment evaluationDetail;
    private Boolean isEvaluationDetailOpened;
    private CompositeSubscription subscriptions;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        this.isEvaluationDetailOpened = false;
        this.setupRecyclerView(courseRecyclerView);
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(this.subscriptions != null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
        if(this.getActivity() != null && isEvaluationDetailOpened) this.getFragmentManager().beginTransaction().remove(evaluationDetail).commit();
        ButterKnife.reset(this);
    }

    @Override
    protected CourseAdapter getAdapter() {
        return new CourseAdapter(this.items, this);
    }

    @Override
    protected RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }

    @Override
    public void recyclerViewListClicked(View view, int position) {
        Timber.d("clicked %s @ %d", view, position);
        if(isEvaluationDetailOpened) return;
        Evaluation.getInstance().update(this.items.get(position));
        this.openEvaluation(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.evaluationDetail = new EvaluationFragment();
        FloatingActionControl.getInstance().setControl(R.layout.fam_course).show(true, 200, TimeUnit.MILLISECONDS);
        this.subscriptions.add(FloatingActionControl
            .clicks(R.id.fab_new_evaluation)
            .subscribe(unused -> jumpToEvaluationStep2())
        );
        this.subscriptions.add(RetrofitApi
                .getInstance()
                .evaluations(
                    User.getInstance().getAccessToken(),
                    User.getInstance().getUniversityId(),
                    null,
                    null,
                    null,
                    Course.getInstance().getId()
                )
                .map(response -> response.evaluations)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(evaluations -> {
                    this.items.clear();
                    this.items.addAll(evaluations);
                    this.adapter.notifyDataSetChanged();
                })
        );
    }

    private void jumpToEvaluationStep2() {
        Evaluation.getInstance().setCourseId(Course.getInstance().getId());
        Evaluation.getInstance().setLectureName(Course.getInstance().getName());
        Evaluation.getInstance().setProfessorName(Course.getInstance().getProfessor());
        this.navigator.navigate(EvaluationStep2Fragment.class, true);
    }

    @Override
    public boolean onBack() {
        if (!isEvaluationDetailOpened) return false;
        if (animators.isRunning()) animators.end();
        this.closeEvaluation();
        return true;
    }

    // Animation
    private Integer top, bottom;
    private Integer screenHeight, itemHeight;
    private static final long ANIMATION_DURATION = 500;
    private AnimatorSet animators;
    private void openEvaluation(View view) {
        this.evaluationContainer.setVisibility(View.VISIBLE);
        if(this.getView() != null) this.screenHeight = this.getView().getHeight();
        this.itemHeight = view.getHeight();
        this.top = (int) view.getY();
        this.bottom = this.top + this.itemHeight;

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(this.top, View.MeasureSpec.UNSPECIFIED);
        this.evaluationContainer.measure(widthSpec, heightSpec);
        this.evaluationContainer.setY((int) view.getY());

        ViewGroup.LayoutParams lpEvaluationContainer = evaluationContainer.getLayoutParams();
        ValueAnimator animHeight = ValueAnimator.ofInt(view.getHeight(), screenHeight);
        animHeight.addUpdateListener(animator -> {
            lpEvaluationContainer.height = (int) animator.getAnimatedValue();
            this.evaluationContainer.setLayoutParams(lpEvaluationContainer);
        });

        ValueAnimator animTop = ValueAnimator.ofInt(top, 0);
        animTop.addUpdateListener(animator -> this.evaluationContainer.setY((int) animator.getAnimatedValue()));

        ValueAnimator animToolbar = ToolbarUtil.getHideAnimator(this.toolbar);

        animators = new AnimatorSet();
        animators.setDuration(ANIMATION_DURATION);
        animators.playTogether(animHeight, animTop, animToolbar);
        animators.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                getFragmentManager().beginTransaction().add(R.id.evaluation_container, evaluationDetail).commit();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isEvaluationDetailOpened = true;
            }
        });
        animators.start();
    }
    private void closeEvaluation() {
        ViewGroup.LayoutParams lpEvaluationContainer = this.evaluationContainer.getLayoutParams();
        ValueAnimator animHeight = ValueAnimator.ofInt(this.screenHeight, this.itemHeight);
        animHeight.addUpdateListener(animator -> {
            lpEvaluationContainer.height = (int) animator.getAnimatedValue();
            this.evaluationContainer.setLayoutParams(lpEvaluationContainer);
        });

        ValueAnimator animTop = ValueAnimator.ofInt(0, this.top);
        animTop.addUpdateListener(animator -> this.evaluationContainer.setY((int) animator.getAnimatedValue()));

        ValueAnimator animToolbar = ToolbarUtil.getShowAnimator(this.toolbar);

        animators = new AnimatorSet();
        animators.setDuration(ANIMATION_DURATION);
        animators.playTogether(animHeight, animTop, animToolbar);
        animators.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                getFragmentManager().beginTransaction().remove(evaluationDetail).commit();
                evaluationContainer.setVisibility(View.GONE);
                isEvaluationDetailOpened = false;
                FloatingActionControl.getInstance().setControl(R.layout.fam_course).show(true, 200, TimeUnit.MILLISECONDS);
                subscriptions.add(FloatingActionControl
                    .clicks(R.id.fab_new_evaluation)
                    .subscribe(unused -> jumpToEvaluationStep2())
                );
            }
        });
        animators.start();
    }
}
