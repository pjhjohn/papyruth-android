package com.montserrat.app.fragment.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.activity.MainActivity;
import com.montserrat.app.adapter.CourseAdapter;
import com.montserrat.app.fragment.nav.NavFragment;
import com.montserrat.app.model.Evaluation;
import com.montserrat.app.model.PartialEvaluation;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class CourseFragment extends RecyclerViewFragment<CourseAdapter, PartialEvaluation> implements MainActivity.onBackPressedListener {
    private ViewPagerController pagerController;
    private NavFragment.OnCategoryClickListener callback;

    @InjectView(R.id.course_title) protected TextView title;
    @InjectView(R.id.course_professor) protected TextView professor;
    @InjectView(R.id.point_overall) protected  TextView pointOverall;
    @InjectView(R.id.point_gpa_satisfaction) protected  TextView pointSatisfaction;
    @InjectView(R.id.point_easiness) protected  TextView pointEasiness;
    @InjectView(R.id.point_clarity) protected  TextView pointClarity;
    @InjectView(R.id.detail_recyclerview) protected RecyclerView evaluationList;
    protected EvaluationFragment evaluationFragment;
    private Boolean openEvaluation;
    private View view;


    @InjectView(R.id.evaluaiton_fragment) protected FrameLayout frameLayout;
    @InjectView(R.id.course_info) protected LinearLayout courseInfo;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
        this.callback = (NavFragment.OnCategoryClickListener) activity;
        ((MainActivity)activity).setOnBackPressedListener(this, true);
    }
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;
    private FragmentTransaction transaction;
    @Override
    public View onCreateView(LayoutInflater infalter, ViewGroup container, Bundle savedInstanceState) {
        view = infalter.inflate(R.layout.fragment_course, container, false);





        this.subscriptions = new CompositeSubscription();
        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        evaluationList = (RecyclerView)view.findViewById(R.id.detail_recyclerview);
        this.setupRecyclerView(evaluationList);
        ButterKnife.inject(this, view);
        //set visibility to GONE
        frameLayout.setVisibility(View.GONE);


        update();
        this.items.clear();
        this.items.add(newEvaluation("1", "comment", 3));
        this.items.add(newEvaluation("1", "comment", 3));
        this.items.add(newEvaluation("1", "comment", 3));
        this.items.add(newEvaluation("1", "comment", 3));
        this.items.add(newEvaluation("1", "comment", 3));
        this.items.add(newEvaluation("1", "comment", 3));
        this.items.add(newEvaluation("1", "comment", 3));
        this.items.add(newEvaluation("1", "comment", 3));
        this.items.add(newEvaluation("1", "comment", 3));
        this.items.add(newEvaluation("1", "comment", 3));
        this.items.add(newEvaluation("1", "comment", 3));
        this.adapter.notifyDataSetChanged();
        this.openEvaluation = false;

        actionBarHeight = 0;

        evaluationFragment = new EvaluationFragment();
//        evaluationFragment.setArguments(this.getActivity().getIntent().getExtras());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        ((MainActivity)this.getActivity()).setOnBackPressedListener(null, true);
    }

    public PartialEvaluation newEvaluation(String userid, String comment, int like){
        PartialEvaluation ev = new PartialEvaluation();
        ev.professor_name = userid;
        ev.body = comment;
        ev.point_overall = like;
        return ev;
    }

    @Override
    protected CourseAdapter getAdapter (List<PartialEvaluation> items) {
        return CourseAdapter.newInstance(this.items, this);
    }

    @Override
    public void recyclerViewListClicked (View v, int position) {
        ((MainActivity)this.getActivity()).setOnBackPressedListener(this, openEvaluation);


        Timber.d("click - posY : %s actionbar : %s", frameLayout.getY(), actionBarHeight);
        if(!openEvaluation){
            PartialEvaluation item = items.get(position);
            Evaluation.getInstance().setCourse_id(item.course_id);
            Evaluation.getInstance().setBody(item.body);
            Evaluation.getInstance().setCreated_at(item.created_at);
            Evaluation.getInstance().setLecture_name(this.title.getText().toString());
            Evaluation.getInstance().setId(item.id);
            Evaluation.getInstance().setUpdated_at(item.updated_at);
            Evaluation.getInstance().setUser_id(item.user_id);
            Evaluation.getInstance().setCourse_id(item.course_id);
            Evaluation.getInstance().setPoint_overall(item.point_overall);
            Evaluation.getInstance().setPoint_easiness(item.point_easiness);
            Evaluation.getInstance().setPoint_gpa_satisfaction(item.point_gpa_satisfaction);
            Evaluation.getInstance().setPoint_clarity(item.point_clarity);
            Evaluation.getInstance().setProfessor_name(item.professor_name);

            transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.evaluaiton_fragment, evaluationFragment);
            expand(v);
            transaction.commit();
            this.openEvaluation = true;
        }else
            onBack();
    }


    //Aniamtion Test
    private Integer bottomLine, topLine;
    private Integer maxHeight, actionBarHeight;
    private Integer bottom, top;

    private final long animationSpeed = 1000;
    private final long animationMaxSpeed = 20;

    private ValueAnimator mAnimator;
    private void expand(View v){
//        Timber.d("hg : %s ,%s,%s", view.getHeight(), view.getY(), );
        //set Visible
        this.frameLayout.setVisibility(View.VISIBLE);
        this.actionBarHeight = (((MainActivity) this.getActivity()).getActionbarHeight());
        this.maxHeight = this.view.getHeight();
        this.topLine = (int)v.getY() + this.courseInfo.getHeight() + actionBarHeight;
        this.bottomLine = this.topLine +v.getHeight();
        this.bottom = this.maxHeight - this.bottomLine;
        this.top = this.topLine - this.actionBarHeight;

        Timber.d("actionBarHeight : %s, maxH : %s , topLine : %s, bottomLine : %s, bottom : %s, top : %s, realPOSY : %s",actionBarHeight, maxHeight, topLine, bottomLine, bottom, top, v.getY());

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(this.topLine, View.MeasureSpec.UNSPECIFIED);
        this.frameLayout.measure(widthSpec, heightSpec);
        this.frameLayout.setY((int) v.getY());

        this.mAnimator = slideAnimator(0, maxHeight);
        this.mAnimator.setDuration(animationSpeed);
        this.mAnimator.start();
    }


    private void collapse() {
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(this.maxHeight, View.MeasureSpec.UNSPECIFIED);
        this.frameLayout.measure(widthSpec, heightSpec);

        this.mAnimator = slideAnimator(maxHeight, 0);
        this.mAnimator.setDuration(animationSpeed);
        this.mAnimator.start();
    }

    private ValueAnimator slideAnimator(int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);

        ViewGroup.LayoutParams layoutParams = frameLayout.getLayoutParams();
        Timber.d("start %s  end %s posY %s", start, end, frameLayout.getY());

        float rate = top/bottom;

        float increaseRate;
        if (rate < 0) {
            increaseRate = animationMaxSpeed;
        } else if (rate < 1) {
            increaseRate = 1;
        } else if (rate > animationMaxSpeed)
            increaseRate = animationMaxSpeed;
        else
            increaseRate = rate;

        if (start < end) {
            frameLayout.getLayoutParams().height = bottomLine-topLine;
            frameLayout.setY(topLine);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    //Update Height

                    int value = (Integer) valueAnimator.getAnimatedValue();
//                if(value < bottomLine-topLine) ;
//                else


                    if (layoutParams.height < maxHeight)
                        layoutParams.height = (int) (bottomLine - topLine + (value * (increaseRate + 1)));
                    else {
                        layoutParams.height = maxHeight;
                        frameLayout.setY(actionBarHeight);
                        return;
                    }

                    frameLayout.setLayoutParams(layoutParams);
                    if (frameLayout.getY() > topLine) ;

                    else if (frameLayout.getY() > actionBarHeight) {
                        frameLayout.setY(topLine - value * increaseRate);
                    } else
                        frameLayout.setY(actionBarHeight);


//                    Timber.d("%s %s %s", value, layoutParams.height, incereaseSpeed);
                }
            });
        }else{
            int decrease = 0;
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int value = (Integer) ((ValueAnimator)valueAnimator).getAnimatedValue();
//                if(value < bottomLine-topLine) ;
//                else

                    Timber.d("coll height : %s, Y : %s", frameLayout.getHeight(), frameLayout.getY());
                    if (frameLayout.getHeight() > bottomLine - topLine)
                        layoutParams.height = (int) (value - (increaseRate + 1)*(maxHeight - value));
                    else {
                        layoutParams.height = bottomLine - topLine;
                        frameLayout.setY(topLine+1);
                        valueAnimator.cancel();
                    }

                    frameLayout.setLayoutParams(layoutParams);
                    if (frameLayout.getY() >= actionBarHeight && (frameLayout.getY() < topLine)) {
                        frameLayout.setY(actionBarHeight + (maxHeight - value) * (increaseRate + 1));
                        Timber.d("unfixed");
                    } else{
                        Timber.d("fixed");
                        frameLayout.setY(topLine+1);
                    }


                    Timber.d("%s %s %s", value, layoutParams.height, increaseRate);
                }
            });
            animator.addListener(new AnimatorListenerAdapter(){
                @Override
                public void onAnimationEnd(Animator valueAnimator) {
                    transaction = getFragmentManager().beginTransaction();
                    transaction.remove(evaluationFragment);
                    transaction.commit();
                }
            });
        }
        Timber.d("layout posY : %s, height : %s, topLine:%s", frameLayout.getY(), frameLayout.getHeight(), topLine);
        return animator;
    }

    @Override
    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }

    public void update(){
        title.setText("computer");
        professor.setText("xxx prof");
        pointOverall.setText(10+"");
        pointSatisfaction.setText(10+"");
        pointEasiness.setText(10+"");
        pointClarity.setText(10+"");
    }


    @Override
    public void onBack() {
        if (openEvaluation){
            if(mAnimator.isRunning())
                mAnimator.end();
            ((MainActivity)this.getActivity()).setOnBackPressedListener(this, openEvaluation);
            collapse();
            this.openEvaluation = false;
        }

    }
}
