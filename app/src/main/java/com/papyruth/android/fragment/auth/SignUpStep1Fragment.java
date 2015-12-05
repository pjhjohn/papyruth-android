package com.papyruth.android.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.PapyruthApplication;
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.model.UniversityData;
import com.papyruth.android.model.unique.SignUpForm;
import com.papyruth.android.recyclerview.adapter.UniversityAdapter;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.navigator.OnBack;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep1Fragment extends Fragment implements RecyclerViewItemClickListener {
    private AuthActivity mActivity;
    private Navigator mNavigator;
    private Tracker mTracker;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AuthActivity) activity;
        mNavigator = (Navigator) activity;
        mTracker = ((PapyruthApplication) mActivity.getApplication()).getTracker();
    }

    @InjectView (R.id.signup_university_recyclerview) protected RecyclerView mUniversityRecyclerView;
    private CompositeSubscription mCompositeSubscription;
    private List<UniversityData> mUniversities;
    private UniversityAdapter mAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step1, container, false);
        ButterKnife.inject(this, view);
        mCompositeSubscription = new CompositeSubscription();

        mUniversities = new ArrayList<>();
        mAdapter = new UniversityAdapter(mUniversities, this);
        mUniversityRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, 2));
        mUniversityRecyclerView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(mCompositeSubscription ==null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_auth_signup1));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mActivity.setCurrentAuthStep(AppConst.Navigator.Auth.SIGNUP_STEP1);
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_next);
        if(SignUpForm.getInstance().getUniversityId() != null && SignUpForm.getInstance().getEntranceYear() != null) FloatingActionControl.getInstance().show(true);
        Observable.timer(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(unused -> {
            if (mUniversityRecyclerView != null)
                ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mUniversityRecyclerView.getWindowToken(), 0);
        });

        mCompositeSubscription.add(FloatingActionControl.clicks().subscribe(unused -> mNavigator.navigate(SignUpStep2Fragment.class, true)));

        Api.papyruth()
            .universities()
            .map(response -> response.universities)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(universities -> {
                mUniversities.clear();
                mUniversities.addAll(universities);
                mAdapter.notifyDataSetChanged();
            }, error -> ErrorHandler.handle(error, this));
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        for(int i = 0; i < mUniversityRecyclerView.getChildCount(); i ++) mUniversityRecyclerView.getChildAt(i).setSelected(false);
        view.setSelected(true);

        SignUpForm.getInstance().setUniversityId(mUniversities.get(position).id);
        SignUpForm.getInstance().setImageUrl(mUniversities.get(position).image_url);

        final int length = Calendar.getInstance().get(Calendar.YEAR) - AppConst.MIN_ENTRANCE_YEAR + 1;
        String[] years = new String[length];
        for(int i = 0; i < length; i ++) years[i] = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - i);
        new MaterialDialog.Builder(mActivity)
            .title(R.string.dialog_title_entrance_year)
            .negativeText(R.string.cancel)
            .buttonsGravity(GravityEnum.START)
            .items(years)
            .itemsCallback((dialog, v, which, text) -> {
                SignUpForm.getInstance().setEntranceYear(Integer.parseInt(text.toString()));
                mNavigator.navigate(SignUpStep2Fragment.class, true);
            })
            .show();
    }
}