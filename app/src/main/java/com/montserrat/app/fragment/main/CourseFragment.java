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
 import com.montserrat.app.model.Evaluation;
 import com.montserrat.app.model.PartialEvaluation;
 import com.montserrat.app.model.User;
 import com.montserrat.utils.support.retrofit.RetrofitApi;
 import com.montserrat.utils.view.fragment.RecyclerViewFragment;
 import com.montserrat.utils.view.viewpager.ViewPagerController;

 import java.util.ArrayList;
 import java.util.List;

 import butterknife.ButterKnife;
 import butterknife.InjectView;
 import rx.android.schedulers.AndroidSchedulers;
 import rx.schedulers.Schedulers;
 import rx.subscriptions.CompositeSubscription;
 import timber.log.Timber;

public class CourseFragment extends RecyclerViewFragment<CourseAdapter, PartialEvaluation> implements MainActivity.onBackPressedListener {
     private ViewPagerController pagerController;
     private NavFragment.OnCategoryClickListener callback;

     @InjectView(R.id.course_title) protected TextView title;
     @InjectView(R.id.course_professor) protected TextView professor;
     @InjectView(R.id.point_overall) protected SeekBar pointOverall;
     @InjectView(R.id.point_gpa_satisfaction) protected  SeekBar pointSatisfaction;
     @InjectView(R.id.point_clarity) protected  SeekBar pointClarity;
     @InjectView(R.id.point_easiness) protected  SeekBar pointEasiness;
     @InjectView(R.id.detail_recyclerview) protected RecyclerView evaluationList;

     @InjectView(R.id.professor_picture) protected ImageView professorPicture;
     @InjectView(R.id.lecture_type) protected TextView lectureType;

     @InjectView(R.id.tags) protected LinearLayout layoutTags;
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

         ButterKnife.inject(this, view);
         pointOverall.setEnabled(false);
         pointSatisfaction.setEnabled(false);
         pointClarity.setEnabled(false);
         pointEasiness.setEnabled(false);



         this.subscriptions = new CompositeSubscription();
         this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
         evaluationList = (RecyclerView)view.findViewById(R.id.detail_recyclerview);
         this.setupRecyclerView(evaluationList);
 //set visibility to GONE
         frameLayout.setVisibility(View.GONE);


         update();
         this.items.clear();
 //         this.adapter.notifyDataSetChanged();

         this.openEvaluation = false;

         actionBarHeight = 0;
         getEvaluations();

         evaluationFragment = new EvaluationFragment();
 //        evaluationFragment.setArguments(this.getActivity().getIntent().getExtras());

         return view;
     }

     @Override
     public void onDestroyView() {
         super.onDestroyView();
         ((MainActivity)this.getActivity()).setOnBackPressedListener(null, true);
         if(this.subscriptions!=null&&!this.subscriptions.isUnsubscribed())this.subscriptions.unsubscribe();
         removeFragment();
         evaluationList.setAdapter(null);
         this.items.clear();
         ButterKnife.reset(this);
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
 //        Timber.d("position : %s", position);
 //        Timber.d("click - posY : %s actionbar : %s", frameLayout.getY(), actionBarHeight);
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
 //            Evaluation.getInstance().setLike(item.like);
 //            Evaluation.getInstance().setUser_name(item.user_name);

             transaction = getFragmentManager().beginTransaction();
             transaction.add(R.id.evaluaiton_fragment, evaluationFragment);
             expand(v);
             transaction.commit();
             this.openEvaluation = true;
         }else
             onBack();
     }

     private void removeFragment(){
         transaction = getFragmentManager().beginTransaction();
         transaction.remove(evaluationFragment);
         transaction.commit();
     }



     @Override
     public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
         return new LinearLayoutManager(this.getActivity());
     }

     @Override
     public void onBack() {
         if (openEvaluation){
             if(animators.isRunning())
                 animators.end();
             ((MainActivity)this.getActivity()).setOnBackPressedListener(this, openEvaluation);
             collapse();
             this.openEvaluation = false;
         }
     }
     public void update(){
         title.setText("computer");
         professor.setText("xxx prof");
         pointOverall.setProgress(3);
         pointSatisfaction.setProgress(7);
         pointClarity.setProgress(5);
         pointEasiness.setProgress(8);

         lectureType.setText(R.string.lecture_type_major);
     }


     public void getEvaluations(){
         subscriptions.add(
                 RetrofitApi.getInstance().evaluations(
                         User.getInstance().getAccessToken(),
                         User.getInstance().getUniversityId(),
                         null, null, null )
                         .map(response -> response.evaluations)
                         .subscribeOn(Schedulers.io())
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribe(evauations -> {
 //                    if(evauations != null)
//                             this.items.addAll(sampleData());
                             this.adapter.notifyDataSetChanged();
                             Timber.d("evaluations : %s", evauations);
                         })
         );
     }

     public List<PartialEvaluation> sampleData(){
         List<PartialEvaluation> evaluations = new ArrayList<>();
         evaluations.add(newEvaluation("1", "comment", 3));
         evaluations.add(newEvaluation("1", "comment", 3));
         evaluations.add(newEvaluation("1", "comment", 3));
         return evaluations;
     }

     //Aniamtion
     private Integer topLine;
     private Integer maxHeight, actionBarHeight, viewHieght;

     private final long ANIMATION_SPEED = 600;

     private AnimatorSet animators;
     private void expand(View v){

         this.frameLayout.setVisibility(View.VISIBLE);
         this.actionBarHeight = (((MainActivity) this.getActivity()).getActionbarHeight());
         this.maxHeight = this.view.getHeight();
         this.topLine = (int)v.getY() + this.courseInfo.getHeight() + actionBarHeight;
         this.viewHieght = v.getHeight();

 //debuging message
 //        Timber.d("actionBarHeight : %s, maxH : %s , topLine : %s, bottomLine : %s, bottom : %s, top : %s, realPOSY : %s",actionBarHeight, maxHeight, topLine, bottomLine, bottom, top, v.getY());

         final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
         final int heightSpec = View.MeasureSpec.makeMeasureSpec(this.topLine, View.MeasureSpec.UNSPECIFIED);
         this.frameLayout.measure(widthSpec, heightSpec);
         this.frameLayout.setY((int) v.getY());


         ViewGroup.LayoutParams layoutParams = frameLayout.getLayoutParams();
         ValueAnimator heightAnimator = ValueAnimator.ofInt(v.getHeight(), maxHeight);
         heightAnimator.addUpdateListener(animation-> {
             layoutParams.height = (int) animation.getAnimatedValue();
             frameLayout.setLayoutParams(layoutParams);
         });

         ValueAnimator positionAnimator = ValueAnimator.ofInt(topLine, actionBarHeight);
         positionAnimator.addUpdateListener(animation-> frameLayout.setY((int) animation.getAnimatedValue()) );

         animators = new AnimatorSet();
         animators.setDuration(ANIMATION_SPEED);
         animators.playTogether(positionAnimator, heightAnimator);
         animators.start();
     }


     private void collapse() {
         ViewGroup.LayoutParams layoutParams = frameLayout.getLayoutParams();
         ValueAnimator heightAnimator = ValueAnimator.ofInt(this.maxHeight, this.viewHieght);
         heightAnimator.addUpdateListener(animation -> {
             layoutParams.height = (int) animation.getAnimatedValue();
             frameLayout.setLayoutParams(layoutParams);
         });

         ValueAnimator positionAnimator = ValueAnimator.ofInt(this.actionBarHeight,this.topLine);
         positionAnimator.addUpdateListener(animation-> {
             frameLayout.setY((int) animation.getAnimatedValue());
         });
         animators = new AnimatorSet();
         animators.setDuration(ANIMATION_SPEED);
         animators.playTogether(positionAnimator, heightAnimator);
         animators.addListener(new AnimatorListenerAdapter() {
             @Override
             public void onAnimationEnd(Animator animation) {
                 super.onAnimationEnd(animation);
                 removeFragment();
             }
         });
         animators.start();
     }
 }
