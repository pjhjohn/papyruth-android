package com.montserrat.app.fragment.main;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.adapter.EvaluationAdapter;
import com.montserrat.app.model.EvaluationForm;
import com.montserrat.app.model.Lecture;
import com.montserrat.app.model.User;
import com.montserrat.utils.etc.RetrofitApi;
import com.montserrat.utils.recycler.RecyclerViewClickListener;
import com.montserrat.utils.viewpager.ViewPagerController;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.RetrofitError;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.montserrat.utils.validator.RxValidator.toString;

/**
 * Created by pjhjohn on 2015-04-26.
 * Searches Lecture for Evaluation on Step 1.
 */
public class EvaluationStep1Fragment extends Fragment implements RecyclerViewClickListener {
    private ViewPagerController pagerController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    /* Bind Views */
    @InjectView(R.id.query) protected EditText query;
    @InjectView(R.id.query_result) protected RecyclerView queryResult;
    @InjectView(R.id.btn_next) protected Button next;

    private List<Lecture> lectures;
    private CompositeSubscription subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step1, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();

        /* View Initialization */
        this.queryResult.setLayoutManager(this.getRecyclerViewLayoutManager());

        /* Event Binding */
        this.next.setOnClickListener(v -> this.pagerController.setCurrentPage(AppConst.ViewPager.Evaluation.EVALUATION_STEP2, true));
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null&&!this.subscriptions.isUnsubscribed())this.subscriptions.unsubscribe();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        subscriptions.add(
            WidgetObservable.text(query)
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .map(toString)
                .map(queryStr -> (String) null) /* Temporarily nullify query to avoid querying */
                .flatMap(queryStr -> RetrofitApi.getInstance().lecturelist(User.getInstance().getAccessToken(), queryStr))
                .map(response -> response.lectures)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    lectures -> {
                        this.lectures = lectures;
                        EvaluationAdapter eAdapter = new EvaluationAdapter(lectures, this);
                        this.queryResult.setAdapter(eAdapter);
//                        this.lectureAdapter.notifyDataSetChanged();
                    },
                    error -> {
                        if (error instanceof RetrofitError) {
                            switch (((RetrofitError) error).getResponse().getStatus()) {
                                default:
                                    Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                            }
                        }
                    }
                )
        );
    }

    @Override
    public void recyclerViewListClicked(View view, int position) {
        Lecture data = lectures.get(position);
        EvaluationForm.getInstance().setLectureName(data.name);
        EvaluationForm.getInstance().setProfessorName("prof");
        EvaluationForm.getInstance().setCourseId(data.id);
        this.pagerController.setCurrentPage(AppConst.ViewPager.Evaluation.EVALUATION_STEP2, true);
    }

    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }
}