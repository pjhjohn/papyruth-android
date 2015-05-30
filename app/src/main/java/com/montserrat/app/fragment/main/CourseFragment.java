package com.montserrat.app.fragment.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.activity.MainActivity;
import com.montserrat.app.adapter.CourseAdapter;
import com.montserrat.app.fragment.nav.NavFragment;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.model.PartialEvaluation;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.viewpager.OnBack;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class CourseFragment extends RecyclerViewFragment<CourseAdapter, PartialEvaluation> implements OnBack, OnPageFocus {
    private ViewPagerController pagerController;
    private NavFragment.OnCategoryClickListener callback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
        this.callback = (NavFragment.OnCategoryClickListener) activity;
    }

    @InjectView(R.id.course_info)                   protected LinearLayout courseInfo;
    @InjectView(R.id.course_picture)                protected ImageView picture;
    @InjectView(R.id.course_type)                   protected TextView type;
    @InjectView(R.id.course_title)                  protected TextView title;
    @InjectView(R.id.course_professor)              protected TextView professor;
    @InjectView(R.id.course_point_overall)          protected SeekBar pointOverall;
    @InjectView(R.id.course_point_gpa_satisfaction) protected SeekBar pointSatisfaction;
    @InjectView(R.id.course_point_clarity)          protected SeekBar pointClarity;
    @InjectView(R.id.course_point_easiness)         protected SeekBar pointEasiness;
    @InjectView(R.id.tags)                          protected LinearLayout tags;

    @InjectView(R.id.evaluations_recyclerview)      protected RecyclerView evaluations;

    @InjectView(R.id.evaluaiton_fragment_container)           protected FrameLayout frameLayout;

    private EvaluationFragment evaluationFragment;
    private Boolean isEvaluationOpened;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;
    private FragmentTransaction transaction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);

        pointOverall.setEnabled(false);
        pointSatisfaction.setEnabled(false);
        pointClarity.setEnabled(false);
        pointEasiness.setEnabled(false);
        frameLayout.setVisibility(View.GONE);

        this.setupRecyclerView(evaluations);
        this.isEvaluationOpened = false;


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (this.subscriptions != null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();

        // Generates : java.lang.RuntimeException: Unable to destroy activity {com.montserrat.main/com.montserrat.app.activity.MainActivity}: java.lang.IllegalStateException: Activity has been destroyed
        if(isEvaluationOpened)
            this.getFragmentManager().beginTransaction().remove(evaluationFragment).commit();
        ButterKnife.reset(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(this.getUserVisibleHint()) this.onPageFocused();

    }


    private List<PartialEvaluation> sampleData() {
        List<PartialEvaluation> evaluations = new ArrayList<>();
        evaluations.add(newEvaluation("1", "comment", 3));
        evaluations.add(newEvaluation("1", "comment", 3));
        evaluations.add(newEvaluation("1", "comment", 3));
        return evaluations;
    }
    private PartialEvaluation newEvaluation(String userid, String comment, int like) {
        PartialEvaluation ev = new PartialEvaluation();
        ev.professor_name = userid;
        ev.body = comment;
        ev.point_overall = like;
        return ev;
    }

    @Override
    protected CourseAdapter getAdapter(List<PartialEvaluation> items) {
        return CourseAdapter.newInstance(this.items, this);
    }

    @Override
    public void recyclerViewListClicked(View view, int position) {
        if (!isEvaluationOpened) {
            Evaluation.getInstance().update(items.get(position));

            this.expandEvaluation(view);
            this.getFragmentManager().beginTransaction().add(R.id.evaluaiton_fragment_container, evaluationFragment).commit();
            this.isEvaluationOpened = true;
        } else Timber.d("recyclerViewListClicked. originally, should've called onBack()");
    }

    @Override
    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }

    @Override
    public boolean onBack() {
        if (!isEvaluationOpened) return false;
        if (animators.isRunning()) animators.end();
        this.collapseEvaluation();
        this.isEvaluationOpened = false;
        return true;
    }

    //Aniamtion
    private Integer topLine;
    private Integer maxHeight, actionBarHeight, viewHieght;
    private final long ANIMATION_SPEED = 600;
    private AnimatorSet animators;

    private void expandEvaluation(View view) {

        this.frameLayout.setVisibility(View.VISIBLE);
        this.actionBarHeight = (((MainActivity) this.getActivity()).getActionbarHeight());
        this.maxHeight = this.getView().getHeight();
        this.topLine = (int) view.getY() + this.courseInfo.getHeight() + actionBarHeight;
        this.viewHieght = view.getHeight();

        //debuging message
        //        Timber.d("actionBarHeight : %s, maxH : %s , topLine : %s, bottomLine : %s, bottom : %s, top : %s, realPOSY : %s",actionBarHeight, maxHeight, topLine, bottomLine, bottom, top, v.getY());

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(this.topLine, View.MeasureSpec.UNSPECIFIED);
        this.frameLayout.measure(widthSpec, heightSpec);
        this.frameLayout.setY((int) view.getY());


        ViewGroup.LayoutParams layoutParams = frameLayout.getLayoutParams();
        ValueAnimator heightAnimator = ValueAnimator.ofInt(view.getHeight(), maxHeight);
        heightAnimator.addUpdateListener(animation -> {
            layoutParams.height = (int) animation.getAnimatedValue();
            frameLayout.setLayoutParams(layoutParams);
        });

        ValueAnimator positionAnimator = ValueAnimator.ofInt(topLine, actionBarHeight);
        positionAnimator.addUpdateListener(animation -> frameLayout.setY((int) animation.getAnimatedValue()));

        animators = new AnimatorSet();
        animators.setDuration(ANIMATION_SPEED);
        animators.playTogether(positionAnimator, heightAnimator);
        animators.start();
    }


    private void collapseEvaluation() {
        ViewGroup.LayoutParams layoutParams = frameLayout.getLayoutParams();
        ValueAnimator heightAnimator = ValueAnimator.ofInt(this.maxHeight, this.viewHieght);
        heightAnimator.addUpdateListener(animation -> {
            layoutParams.height = (int) animation.getAnimatedValue();
            frameLayout.setLayoutParams(layoutParams);
        });

        ValueAnimator positionAnimator = ValueAnimator.ofInt(this.actionBarHeight, this.topLine);
        positionAnimator.addUpdateListener(animation -> {
            frameLayout.setY((int) animation.getAnimatedValue());
        });
        animators = new AnimatorSet();
        animators.setDuration(ANIMATION_SPEED);
        animators.playTogether(positionAnimator, heightAnimator);
        animators.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                CourseFragment.this.getFragmentManager().beginTransaction().remove(evaluationFragment).commit();
            }
        });
        animators.start();
    }

    @Override
    public void onPageFocused() {
        this.title.setText(Course.getInstance().getName());
        this.professor.setText(Course.getInstance().getProfessor());
        this.pointOverall.setProgress(Course.getInstance().getPointOverall() * 100);
        this.pointSatisfaction.setProgress(Course.getInstance().getPointGpaSatisfaction() * 100);
        this.pointClarity.setProgress(Course.getInstance().getPointClarity() * 100);
        this.pointEasiness.setProgress(Course.getInstance().getPointEasiness() * 100);
        this.type.setText(R.string.lecture_type_major);
        Timber.d("progress : %s",this.pointClarity.getProgress());

        this.subscriptions.add(
                RetrofitApi.getInstance().evaluations(
                        User.getInstance().getAccessToken(),
                        User.getInstance().getUniversityId(),
                        null, null, null, null)
                        .map(response -> response.evaluations)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(evauations -> {
                            this.items.clear();
                            this.items.addAll(evauations);
                            this.adapter.notifyDataSetChanged();
                        })
        );
        this.evaluationFragment = new EvaluationFragment();
    }
}
