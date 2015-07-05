package com.montserrat.app.fragment.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.recyclerview.adapter.CourseAdapter;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.model.unique.EvaluationForm;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.MetricUtil;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.viewpager.OnBack;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
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
    private EvaluationFragment evaluationDetail;
    private Boolean isEvaluationDetailOpened;
    private CompositeSubscription subscriptions;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        this.isEvaluationDetailOpened = false;
        this.setupRecyclerView(this.courseRecyclerView);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(this.subscriptions != null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
        if(this.getActivity() != null && this.isEvaluationDetailOpened) this.getFragmentManager().beginTransaction().remove(evaluationDetail).commit();
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
    public void onRecyclerViewItemClick(View view, int position) {
        Timber.d("clicked %s @ %d", view, position);
        if(isEvaluationDetailOpened) return;
        if(animators != null && animators.isRunning()) return;
        Evaluation.getInstance().update(this.items.get(position));
        ((LinearLayoutManager)this.courseRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
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
            .get_evaluations(
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
                this.adapter.notifyItemRangeChanged(2, this.adapter.getItemCount() - 2);
                if (this.navigator.getBackStackNameAt(1).equals(HomeFragment.class.getSimpleName())) {
                    this.selectEvaluation();
                }
            }, error -> Timber.d("get Evaluation Error %s", error)
            )
        );
    }

    private void jumpToEvaluationStep2() {
        EvaluationForm.getInstance().setCourseId(Course.getInstance().getId());
        EvaluationForm.getInstance().setLectureName(Course.getInstance().getName());
        EvaluationForm.getInstance().setProfessorName(Course.getInstance().getProfessor());
        this.navigator.navigate(EvaluationStep2Fragment.class, true);
    }

    private void selectEvaluation(){
        Bundle bundle = this.getArguments();
        Integer evaluationId = bundle.getInt("evaluationid");
        for(int i = 0; i < items.size(); i++){
            if(this.items.get(i).id.equals(evaluationId)){
                final int position = i;
                this.subscriptions.add(
                    Observable
                        .timer(1000, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(un ->
                                onRecyclerViewItemClick(this.courseRecyclerView.getChildAt(position+2), position),
                            error -> Timber.d("evaluation open error : %s", error)
                        )
                );
                break;
            }
        }
    }

    @Override
    public boolean onBack() {
        if (!isEvaluationDetailOpened && animators == null) return false;
        if (!isEvaluationDetailOpened && !animators.isRunning()) return false;
        if (!isEvaluationDetailOpened ) animators.cancel();
        else if(animators.isRunning()) animators.end();
        else if(!evaluationDetail.onBack()) this.closeEvaluation();
        return true;
    }

    // Animation
    private Integer itemTop, itemHeight, screenHeight;
    private AnimatorSet animators;
    private Boolean isAnimationCanceled;
    private void openEvaluation(View view) {
        this.evaluationContainer.setVisibility(View.VISIBLE);
        if(this.getView() != null) this.screenHeight = this.getView().getHeight();
        this.itemHeight = view.getHeight();
        this.itemTop = (int) view.getY();

        ViewGroup.LayoutParams lpEvaluationContainer = evaluationContainer.getLayoutParams();
        ValueAnimator animHeight = ValueAnimator.ofInt(view.getHeight(), screenHeight);
        animHeight.addUpdateListener(animator -> {
            lpEvaluationContainer.height = (int) animator.getAnimatedValue();
            this.evaluationContainer.setLayoutParams(lpEvaluationContainer);
        });
        animHeight.setInterpolator(new AccelerateInterpolator(AppConst.ANIM_DECELERATION));

        ValueAnimator animTop = ValueAnimator.ofInt(this.itemTop, 0);
        animTop.addUpdateListener(animator -> {
            final int itemTop = (int) animator.getAnimatedValue();
            this.evaluationContainer.setY(itemTop);
            final int toolbarTop = itemTop - MetricUtil.getPixels(this.toolbar.getContext(), R.attr.actionBarSize);
            this.toolbar.setY(toolbarTop >= 0 ? 0 : toolbarTop);
        });
        animTop.setInterpolator(new AccelerateInterpolator(AppConst.ANIM_DECELERATION));

        animators = new AnimatorSet();
        animators.setDuration(AppConst.ANIM_DURATION_MEDIUM);
        animators.playTogether(animHeight, animTop);
        animators.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                getFragmentManager().beginTransaction().add(R.id.evaluation_container, evaluationDetail).commit();
                isAnimationCanceled = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                getFragmentManager().beginTransaction().remove(evaluationDetail).commit();
                isAnimationCanceled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (isAnimationCanceled) toolbar.setY(0);
                else {
                    isEvaluationDetailOpened = true;
                    evaluationDetail.setEvaluationFloatingActionControl();
                }
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
        animHeight.setInterpolator(new DecelerateInterpolator(AppConst.ANIM_ACCELERATION));

        ValueAnimator animTop = ValueAnimator.ofInt(0, this.itemTop);
        animTop.addUpdateListener(animator -> {
            final int itemTop = (int) animator.getAnimatedValue();
            this.evaluationContainer.setY(itemTop);
            final int toolbarTop = itemTop - MetricUtil.getPixels(toolbar.getContext(), R.attr.actionBarSize);
            this.toolbar.setY(toolbarTop >= 0 ? 0 : toolbarTop);
        });
        animTop.setInterpolator(new DecelerateInterpolator(AppConst.ANIM_ACCELERATION));

        animators = new AnimatorSet();
        animators.setDuration(AppConst.ANIM_DURATION_MEDIUM);
        animators.playTogether(animHeight, animTop);
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
