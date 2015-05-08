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
import java.util.zip.Inflater;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

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

    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {

        View view = inflater.inflate(R.layout.fragment_evaluation_step1, container, false);
        ButterKnife.inject(this, view);
        this.vLectureList.setLayoutManager(this.getRecyclerViewLayoutManager());

        /* Bind Events */
        this.btnNext.setOnClickListener(v -> this.pagerController.setCurrentPage(AppConst.ViewPager.Evaluation.EVALUATION_STEP2, true));

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        subscriptions.add(
            WidgetObservable.text(vLectureQuery)
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
                    List<EvaluationAdapter.Holder.Data> lectures = new ArrayList<>();
                    for (int i = 0; i < jsonarray.length(); i++){
                        try {
                            EvaluationAdapter.Holder.Data data = new EvaluationAdapter.Holder.Data(
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        this.subscriptions.unsubscribe();
    }

    public View validate() {
        List<View> vFailed = new ArrayList<View>();
        View candidate;
        // TODO : pick candidates failed to validate certain validation rule.
        return vFailed.isEmpty() ? null : vFailed.get(0);
    }

    public static Fragment newInstance() {
        Fragment fragment = new EvaluationStep1Fragment();
        Bundle bundle = new Bundle();
        /* For AutoComplete TextView for lecture title & professor name */
        bundle.putString(AppConst.Request.API_ROOT_URL, AppConst.API_ROOT);
        bundle.putString(AppConst.Request.API_VERSION, AppConst.API_VERSION);
        bundle.putString(AppConst.Request.ACTION, "");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_evaluation_step1);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void recyclerViewListClicked(View view, int position) {

    }

    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }
}