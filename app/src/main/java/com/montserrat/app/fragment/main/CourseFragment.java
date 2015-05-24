package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
        this.callback = (NavFragment.OnCategoryClickListener) activity;
        ((MainActivity)activity).setOnBackPressedListener(this);
    }
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;
    private FragmentTransaction transaction;
    @Override
    public View onCreateView(LayoutInflater infalter, ViewGroup container, Bundle savedInstanceState) {
        View view = infalter.inflate(R.layout.fragment_course, container, false);
        this.subscriptions = new CompositeSubscription();
        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        evaluationList = (RecyclerView)view.findViewById(R.id.detail_recyclerview);
        this.setupRecyclerView(evaluationList);
        ButterKnife.inject(this, view);
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

        evaluationFragment = new EvaluationFragment();
//        evaluationFragment.setArguments(this.getActivity().getIntent().getExtras());


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        ((MainActivity)this.getActivity()).setOnBackPressedListener(null);
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
    public void recyclerViewListClicked (View view, int position) {
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
//            Evaluation.getInstance().getUser_name(item.);

            transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.evaluaiton_fragment, evaluationFragment);
//            transaction.addToBackStack(null);
            transaction.commit();
            this.openEvaluation = true;
        }
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
            transaction = getFragmentManager().beginTransaction();
            transaction.remove(evaluationFragment);
            transaction.commit();
            this.openEvaluation = false;
        }
    }
}
