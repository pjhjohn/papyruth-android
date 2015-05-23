package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.adapter.CourseAdapter;
import com.montserrat.app.fragment.nav.NavFragment;
import com.montserrat.app.model.PartialEvaluation;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class CourseFragment extends RecyclerViewFragment<CourseAdapter, PartialEvaluation>{
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


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
        this.callback = (NavFragment.OnCategoryClickListener) activity;
    }
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;

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

        evaluationFragment = new EvaluationFragment();
//        evaluationFragment.setArguments(this.getActivity().getIntent().getExtras());

        //Evaluation 플래그먼트를 현재 위치 위에 올릴 수 있게 됨.
        //Click 이벤트시 해당 강의평을 가져와 상단에 올리게 하면 됨
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.evaluaiton_fragment, evaluationFragment);
        Timber.d("eva frag : %s", evaluationFragment.getId());
//        ft.hide(evaluationFragment);
//        ft.remove(evaluationFragment);
        ft.commit();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    public PartialEvaluation newEvaluation(String userid, String comment, int like){
        PartialEvaluation ev = new PartialEvaluation();
        ev.professor_name = userid;
        ev.comment = comment;
        ev.point_overall = like;
        return ev;
    }

    @Override
    protected CourseAdapter getAdapter (List<PartialEvaluation> items) {
        return CourseAdapter.newInstance(this.items, this);
    }

    @Override
    public void recyclerViewListClicked (View view, int position) {
//        FragmentManager fragmentManager = getFragmentManager();
//        EvaluationFragment evaluationFragment = new EvaluationFragment();
//        Bundle bundle = new Bundle();
//        evaluationFragment.setArguments(bundle);
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.add(R.id.evaluaiton_fragment,evaluationFragment);
//
//        fragmentTransaction.commit();
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
}
