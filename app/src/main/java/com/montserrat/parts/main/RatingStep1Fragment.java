package com.montserrat.parts.main;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.parts.auth.User;
import com.montserrat.utils.request.Api;
import com.montserrat.utils.request.ClientFragment;
import com.montserrat.utils.request.RxVolley;
import com.montserrat.utils.viewpager.ViewPagerController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-26.
 * Searches Lecture for Rating on Step 1.
 */
public class RatingStep1Fragment extends ClientFragment {
    private ViewPagerController pagerController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    private EditText vLectureQuery;
    private ListView vLectureList;
    private Button btnNext;

    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = super.onCreateView(inflater, container, args);

        /* Bind Views */
        this.vLectureQuery = (EditText) view.findViewById(R.id.autotext_lecture);
        this.vLectureList = (ListView) view.findViewById(R.id.result_lecture);
        this.btnNext = (Button) view.findViewById(R.id.btn_next);

        /* Bind Events */
        this.btnNext.setOnClickListener(v -> this.pagerController.setCurrentPage(AppConst.ViewPager.Rating.RATING_STEP2, true));

        return view;
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
                    List<String> lectures = new ArrayList<>();
                    for (int i = 0; i < jsonarray.length(); i++) lectures.add(((JSONObject)jsonarray.opt(i)).optString("name"));
                    return lectures;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    lectures -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            RatingStep1Fragment.this.getActivity(),
                            android.R.layout.simple_list_item_activated_1,
                            lectures
                        );
                        this.vLectureList.setAdapter(adapter);
//                        this.lectureAdapter.notifyDataSetChanged();
                    }, Throwable::printStackTrace
                )
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.subscriptions.unsubscribe();
    }

    public View validate() {
        List<View> vFailed = new ArrayList<View>();
        View candidate;
        // TODO : pick candidates failed to validate certain validation rule.
        return vFailed.isEmpty() ? null : vFailed.get(0);
    }

    public static Fragment newInstance() {
        Fragment fragment = new RatingStep1Fragment();
        Bundle bundle = new Bundle();
        /* For AutoComplete TextView for lecture title & professor name */
        bundle.putString(AppConst.Request.API_ROOT_URL, AppConst.API_ROOT);
        bundle.putString(AppConst.Request.API_VERSION, AppConst.API_VERSION);
        bundle.putString(AppConst.Request.ACTION, "");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_rating_step1);
        fragment.setArguments(bundle);
        return fragment;
    }
}