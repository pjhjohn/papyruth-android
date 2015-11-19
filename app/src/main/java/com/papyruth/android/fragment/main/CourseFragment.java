package com.papyruth.android.fragment.main;

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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.model.unique.Course;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.papyruth;
import com.papyruth.android.recyclerview.adapter.CourseAdapter;
import com.papyruth.utils.support.error.ErrorHandler;
import com.papyruth.utils.support.fab.FloatingActionControl;
import com.papyruth.utils.support.materialdialog.AlertDialog;
import com.papyruth.utils.support.retrofit.apis.Api;
import com.papyruth.utils.view.MetricUtil;
import com.papyruth.utils.view.ToolbarUtil;
import com.papyruth.utils.view.fragment.RecyclerViewFragment;
import com.papyruth.utils.view.navigator.Navigator;
import com.papyruth.utils.view.viewpager.OnBack;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.RetrofitError;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class CourseFragment extends RecyclerViewFragment<CourseAdapter, EvaluationData> implements OnBack {
    private Navigator navigator;
    private Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = ((papyruth) getActivity().getApplication()).getTracker();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.isOpenSlave = false;
        this.navigator = (Navigator) activity;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        this.navigator = null;
    }

    @InjectView(R.id.course_recyclerview) protected RecyclerView courseRecyclerView;
    private Toolbar toolbar;
    private CompositeSubscription subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        this.setupRecyclerView(this.courseRecyclerView);


        this.slave = null;
        this.slaveIsOccupying = false;

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(this.subscriptions != null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
        if(this.getActivity() != null && this.slaveIsOccupying && slave != null) this.getFragmentManager().beginTransaction().remove(slave).commit();
        ButterKnife.reset(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        this.toolbar.setTitle(R.string.toolbar_title_course);
        ToolbarUtil.getColorTransitionAnimator(toolbar, R.color.colorchip_green).start();
        FloatingActionControl.getInstance().setControl(R.layout.fam_course).show(true, 200, TimeUnit.MILLISECONDS);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, false);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, true);
        FloatingActionControl.clicks(R.id.fab_new_evaluation).subscribe(unused -> navigateToEvaluationForm()
            , error->ErrorHandler.throwError(error, this));

        if(User.getInstance().needMoreEvaluation()) {
            AlertDialog.show(getActivity(), navigator, AlertDialog.Type.EVALUATION_MANDATORY);
        }else{
            Api.papyruth()
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
                    final int offset = this.adapter.getItemOffset();
                    this.adapter.setIsEmptyData(evaluations.isEmpty());
                    this.adapter.notifyItemRangeChanged(offset, this.adapter.getItemCount() - offset);
                }, error-> ErrorHandler.throwError(error, this));
        }
    }

    @InjectView(R.id.evaluation_container) protected FrameLayout slaveContainer;
    private EvaluationFragment slave;
    private boolean slaveIsOccupying;
    private Boolean isOpenSlave;

    @Override
    public boolean onBack() {
        if (!slaveIsOccupying && animators == null) return false;
        if (!slaveIsOccupying && !animators.isRunning()) return false;
        if (!slaveIsOccupying ) animators.cancel();
        else if(animators.isRunning()) animators.end();
        else if(!slave.onBack()) this.closeEvaluation();
        return true;
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        if(User.getInstance().needMoreEvaluation()) AlertDialog.show(getActivity(), navigator, AlertDialog.Type.EVALUATION_MANDATORY);
        if(slaveIsOccupying) return;
        if(animators != null && animators.isRunning()) return;
        if(isOpenSlave) return;
        isOpenSlave = true;
        Api.papyruth()
            .get_evaluation(User.getInstance().getAccessToken(), this.items.get(position).id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(response -> {
                Evaluation.getInstance().update(response.evaluation);
                this.slave = new EvaluationFragment();
                this.openEvaluation(view);
            }, error -> ErrorHandler.throwError(error, this));
    }

    /* Animating Slave Views : open & close */
    private Integer itemTop, itemHeight, screenHeight;
    private AnimatorSet animators;
    private Boolean isAnimationCanceled;
    private void openEvaluation(View view) {
        this.isOpenSlave = true;
        this.slaveContainer.setVisibility(View.VISIBLE);
        if(this.getView() != null) this.screenHeight = this.getView().getHeight();
        this.itemHeight = view.getHeight();
        this.itemTop = (int) view.getY();

        ViewGroup.LayoutParams lp = slaveContainer.getLayoutParams();
        ValueAnimator animHeight = ValueAnimator.ofInt(view.getHeight(), screenHeight);
        animHeight.addUpdateListener(animator -> {
            lp.height = (int) animator.getAnimatedValue();
            this.slaveContainer.setLayoutParams(lp);
        });

        ValueAnimator animTop = ValueAnimator.ofInt(this.itemTop, 0);
        animTop.addUpdateListener(animator -> {
            final int itemTop = (int) animator.getAnimatedValue();
            this.slaveContainer.setY(itemTop);
            final int toolbarTop = itemTop - MetricUtil.getPixels(this.toolbar.getContext(), R.attr.actionBarSize);
            this.toolbar.setY(toolbarTop >= 0 ? 0 : toolbarTop);
        });

        animators = new AnimatorSet();
        animators.setDuration(AppConst.ANIM_DURATION_MEDIUM);
        animators.playTogether(animHeight, animTop);
        animators.setInterpolator(new DecelerateInterpolator(AppConst.ANIM_DECELERATION));
        animators.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if(slave != null) getFragmentManager().beginTransaction().add(R.id.evaluation_container, slave).commit();
                isAnimationCanceled = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                if(slave != null) getFragmentManager().beginTransaction().remove(slave).commit();
                isAnimationCanceled = true;
                isOpenSlave = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (isAnimationCanceled) {
                    toolbar.setY(0);
                } else {
                    slaveIsOccupying = true;
                    slave.setEvaluationFloatingActionControl();
                    slave.showContent(true);
                }
            }
        });
        animators.start();
    }
    private void closeEvaluation() {
        this.isOpenSlave = false;
        ViewGroup.LayoutParams lp = this.slaveContainer.getLayoutParams();
        ValueAnimator animHeight = ValueAnimator.ofInt(this.screenHeight, this.itemHeight);
        animHeight.addUpdateListener(animator -> {
            lp.height = (int) animator.getAnimatedValue();
            this.slaveContainer.setLayoutParams(lp);
        });

        ValueAnimator animTop = ValueAnimator.ofInt(0, this.itemTop);
        animTop.addUpdateListener(animator -> {
            final int itemTop = (int) animator.getAnimatedValue();
            this.slaveContainer.setY(itemTop);
            final int toolbarTop = itemTop - MetricUtil.getPixels(toolbar.getContext(), R.attr.actionBarSize);
            this.toolbar.setY(toolbarTop >= 0 ? 0 : toolbarTop);
        });

        animators = new AnimatorSet();
        animators.setDuration(AppConst.ANIM_DURATION_MEDIUM);
        animators.playTogether(animHeight, animTop);
        animators.setInterpolator(new AccelerateInterpolator(AppConst.ANIM_ACCELERATION));
        animators.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                slave.showContent(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                getFragmentManager().beginTransaction().remove(slave).commit();
                slaveContainer.setVisibility(View.GONE);
                slaveIsOccupying = false;
                FloatingActionControl.getInstance().setControl(R.layout.fam_course).show(true, 200, TimeUnit.MILLISECONDS);
                FloatingActionControl.clicks(R.id.fab_new_evaluation).subscribe(unused -> navigateToEvaluationForm());
            }
        });
        animators.start();
    }

    @Override
    protected CourseAdapter getAdapter() {
        return new CourseAdapter(this.items, this);
    }

    @Override
    protected RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }

    private void navigateToEvaluationForm() {
        EvaluationForm.getInstance().setCourseId(Course.getInstance().getId());
        EvaluationForm.getInstance().setLectureName(Course.getInstance().getName());
        EvaluationForm.getInstance().setProfessorName(Course.getInstance().getProfessorName());

        Api.papyruth().post_evaluation_possible(User.getInstance().getAccessToken(), Evaluation.getInstance().getCourseId())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(response -> {
                if (response.success) {
                    ((MainActivity) this.getActivity()).navigate(EvaluationStep2Fragment.class, true);
                } else {
                    EvaluationForm.getInstance().setEvaluationId(response.evaluation_id);
                    AlertDialog.build(getActivity(), navigator, AlertDialog.Type.EVALUATION_POSSIBLE)
                        .show();
                }
            }, error -> ErrorHandler.throwError(error, this));
    }

}
