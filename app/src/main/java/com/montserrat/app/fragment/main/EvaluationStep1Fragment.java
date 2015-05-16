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
import com.montserrat.app.model.User;
import com.montserrat.utils.recycler.RecyclerViewClickListener;
import com.montserrat.utils.request.Api;
import com.montserrat.utils.request.RxVolley;
import com.montserrat.utils.viewpager.ViewPagerController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static rx.android.widget.WidgetObservable.text;

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
    @InjectView(R.id.autotext_lecture) protected EditText vLectureQuery;
    @InjectView(R.id.result_lecture) protected RecyclerView vLectureList;
    @InjectView(R.id.btn_next) protected Button btnNext;

    private EvaluationAdapter.Holder.Data data;
    private List<EvaluationAdapter.Holder.Data> lectures;
    private EvaluationForm eval;
    private CompositeSubscription subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step1, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.vLectureList.setLayoutManager(this.getRecyclerViewLayoutManager());

        /* Bind Events */
        this.btnNext.setOnClickListener(v -> this.pagerController.setCurrentPage(AppConst.ViewPager.Evaluation.EVALUATION_STEP2, true));
        getFragmentManager().beginTransaction().add(this, AppConst.Tag.Evaluation.EVALUATION_STEP1);
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
            text(vLectureQuery)
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMap(onTextChangeEvent -> {
                    try {
                        return RxVolley.createObservable(Api.url("lectures/dummy_autocomplete"), User.getInstance().getAccessToken(), new JSONObject().put("query", onTextChangeEvent.text().toString()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(response -> response != null) // JSONException
                .map(response -> response.optJSONArray("lectures"))
                .filter(jsonarray -> jsonarray != null) // Wrong Data
                .map(jsonarray -> {
                    lectures = new ArrayList<>();
                    for (int i = 0; i < jsonarray.length(); i++){
                        try {
                            data = new EvaluationAdapter.Holder.Data(
                                    ((JSONObject)jsonarray.opt(i)).getString("name"),
                                    "prof",
                                    ((JSONObject)jsonarray.opt(i)).getInt("id"),
                                    0
                            );
                            lectures.add(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    return lectures;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    lectures -> {
                        EvaluationAdapter eAdapter = new EvaluationAdapter(lectures, this);
                        this.vLectureList.setAdapter(eAdapter);
//                        this.lectureAdapter.notifyDataSetChanged();
                    }, Throwable::printStackTrace
                )
        );
    }

    public View validate() {
        List<View> vFailed = new ArrayList<View>();
        View candidate;
        // TODO : pick candidates failed to validate certain validation rule.
        return vFailed.isEmpty() ? null : vFailed.get(0);
    }

    @Override
    public void recyclerViewListClicked(View view, int position) {
        EvaluationAdapter.Holder.Data data = lectures.get(position);
        EvaluationForm.getInstance().setLectureTitle(data.titleText);
        EvaluationForm.getInstance().setProfessorName(data.profText);
        EvaluationStep2Fragment nextStep = (EvaluationStep2Fragment)getActivity().getFragmentManager().findFragmentByTag(AppConst.Tag.Evaluation.EVALUATION_STEP2);
        nextStep.update();
        this.pagerController.setCurrentPage(AppConst.ViewPager.Evaluation.EVALUATION_STEP2, true);
    }

    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }
}