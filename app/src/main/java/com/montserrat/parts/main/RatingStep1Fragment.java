package com.montserrat.parts.main;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.controller.AppManager;
import com.montserrat.parts.auth.UserInfo;
import com.montserrat.utils.request.ClientFragment;
import com.montserrat.utils.request.RxVolley;
import com.montserrat.utils.viewpager.ViewPagerController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
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

    private Button btnNext;
    private AutoCompleteTextView vAutoComplete;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private ArrayAdapter<String> lectureAdapter;
    private List<String> lectureItems;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = super.onCreateView(inflater, container, args);

        /* Bind Views */
        this.vAutoComplete = (AutoCompleteTextView) view.findViewById(R.id.autotext_lecture);
        this.btnNext = (Button) view.findViewById(R.id.btn_next);

        /* Bind Events */
        this.lectureItems = new ArrayList<>();
        this.lectureAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_dropdown_item_1line, this.lectureItems);
        this.vAutoComplete.setAdapter(this.lectureAdapter);
        this.btnNext.setOnClickListener(v -> this.pagerController.setCurrentPage(AppConst.ViewPager.Rating.RATING_STEP2, true));

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        subscriptions.add(
            WidgetObservable.text(vAutoComplete)
                .debounce(500, TimeUnit.MILLISECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    text -> {
                        Log.d("DEBUG", "onNext : Generate RxVolley Request with " + text.text());
                        try {
                            RxVolley.createObservable(
                                "http://mont.izz.kr:3001/api/v1/lectures/dummy_autocomplete",
                                UserInfo.getInstance().getAccessToken(),
                                new JSONObject().put("query", text.text())
                            )
                                .map(response -> response.optJSONArray("lectures"))
                                .filter(jsonarray -> jsonarray != null)
                                .map(jsonarray -> {
                                    this.lectureItems.clear();
                                    for (int i = 0; i < jsonarray.length(); i++)
//                                                        this.lectureItems.add(new Lecture((JSONObject) jsonarray.opt(i)));
                                        this.lectureItems.add(((JSONObject) jsonarray.opt(i)).optString("name"));
                                    return this.lectureItems;
                                })
                                .subscribeOn(Schedulers.io()) /* performed in background till here */
                                .observeOn(AndroidSchedulers.mainThread()). /* performed in UI-thread from here */
                                subscribe(
                                lectures -> {
                                    Log.d("DEBUG", "onNext : Received data : " + lectures);
                                    Log.d("DEBUG", "lectureItems length : " + this.lectureItems.size());
                                    Log.d("DEBUG", "lectureAdapter length : " + this.lectureAdapter.getCount());
                                    this.lectureAdapter.notifyDataSetChanged();
                                }, Throwable::printStackTrace
                            );
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    })
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