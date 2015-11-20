package com.papyruth.android.fragment.auth;

import android.app.Activity;
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
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.model.UniversityData;
import com.papyruth.android.model.unique.SignUpForm;
import com.papyruth.android.papyruth;
import com.papyruth.android.recyclerview.adapter.UniversityAdapter;
import com.papyruth.utils.support.error.ErrorHandler;
import com.papyruth.utils.support.fab.FloatingActionControl;
import com.papyruth.utils.support.retrofit.apis.Api;
import com.papyruth.utils.view.fragment.RecyclerViewFragment;
import com.papyruth.utils.view.viewpager.OnPageFocus;
import com.papyruth.utils.view.viewpager.OnPageUnfocus;
import com.papyruth.utils.view.viewpager.ViewPagerController;

import java.util.Calendar;
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

public class SignUpStep1Fragment extends RecyclerViewFragment<UniversityAdapter, UniversityData> implements OnPageFocus, OnPageUnfocus {
    private ViewPagerController mViewPagerController;
    private Context mContext;
    private Tracker mTracker;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = ((papyruth) getActivity().getApplication()).getTracker();
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mViewPagerController = ((AuthActivity) activity).getViewPagerController();
        mContext = activity;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mViewPagerController = null;
        mContext = null;
    }

    @InjectView (R.id.signup_univ_recyclerview) protected RecyclerView mUniversityRecyclerView;
    private CompositeSubscription mCompositeSubscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step1, container, false);
        ButterKnife.inject(this, view);
        mCompositeSubscription = new CompositeSubscription();
        this.setupRecyclerView(mUniversityRecyclerView);

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
        mCompositeSubscription.add(Api.papyruth()
            .universities()
            .map(response -> response.universities)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(universities -> {
                items.clear();
                items.addAll(universities);
                adapter.notifyDataSetChanged();
            }, error -> ErrorHandler.throwError(error, this))
        );
    }

    @Override
    public void onPageFocused() {
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_next);
//        getActivity().findViewById(R.id.app_logo_horizontal).setVisibility(View.GONE);
        ((AuthActivity) getActivity()).setOnShowSoftKeyboard(null);
        ((AuthActivity) getActivity()).setOnHideSoftKeyboard(null);
        if(SignUpForm.getInstance().getUniversityId() != null && SignUpForm.getInstance().getEntranceYear() != null) FloatingActionControl.getInstance().show(true);
        if(mCompositeSubscription != null && mCompositeSubscription.isUnsubscribed()) mCompositeSubscription = new CompositeSubscription();

        mCompositeSubscription.add(FloatingActionControl.clicks().subscribe(
            unused -> {
                InputMethodManager imm = ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE));
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                imm.showSoftInput(mUniversityRecyclerView, InputMethodManager.SHOW_FORCED);
                /* TODO : Really need this? */
                mCompositeSubscription.add(Observable
                    .timer(300, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        unused2 -> mViewPagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP2, true),
                        error -> ErrorHandler.throwError(error, this)
                    )
                );
            }, error -> ErrorHandler.throwError(error, this)
        ));

        InputMethodManager imm = ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE));
        imm.hideSoftInputFromWindow(mUniversityRecyclerView.getWindowToken(), 0);
    }

    @Override
    public void onPageUnfocused() {
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    protected UniversityAdapter getAdapter () {
        return new UniversityAdapter(items, this);
    }

    @Override
    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new GridLayoutManager(mContext, 2);
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        for(int i = 0; i < mUniversityRecyclerView.getChildCount(); i ++) mUniversityRecyclerView.getChildAt(i).setSelected(false);
        view.setSelected(true);

        SignUpForm.getInstance().setUniversityId(items.get(position).id);
        SignUpForm.getInstance().setImageUrl(items.get(position).image_url);

        final int length = Calendar.getInstance().get(Calendar.YEAR) - AppConst.MIN_ENTRANCE_YEAR + 1;
        String[] years = new String[length];
        for(int i = 0; i < length; i ++) years[i] = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - i);
        new MaterialDialog.Builder(mContext)
            .title(R.string.dialog_title_entrance_year)
            .negativeText(R.string.cancel)
            .buttonsGravity(GravityEnum.START)
            .items(years)
            .itemsCallback((dialog, v, which, text) -> {
                SignUpForm.getInstance().setEntranceYear(Integer.parseInt(text.toString()));
                InputMethodManager imm = ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE));
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                imm.showSoftInput(mUniversityRecyclerView, InputMethodManager.SHOW_FORCED);
                /* TODO : Really need this? */
                mCompositeSubscription.add(Observable
                        .timer(300, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                            unused -> mViewPagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP2, true),
                            error -> ErrorHandler.throwError(error, this)
                        )
                );
            })
            .build()
            .show();
    }
}