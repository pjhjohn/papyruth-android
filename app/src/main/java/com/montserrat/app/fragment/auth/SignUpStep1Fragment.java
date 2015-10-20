package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.activity.AuthActivity;
import com.montserrat.app.model.UniversityData;
import com.montserrat.app.model.unique.SignUpForm;
import com.montserrat.app.recyclerview.adapter.UniversityAdapter;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.OnPageUnfocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep1Fragment extends RecyclerViewFragment<UniversityAdapter, UniversityData> implements OnPageFocus, OnPageUnfocus {
    private ViewPagerController pagerController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = ((AuthActivity) activity).getViewPagerController();
    }
    @Override
    public void onDetach() {
        super.onDetach();
        this.pagerController = null;
    }

    @InjectView (R.id.signup_univ_recyclerview) protected RecyclerView universityList;
    private CompositeSubscription subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step1, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.setupRecyclerView(this.universityList);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.subscriptions.add(RetrofitApi.getInstance()
            .universities()
            .map(response -> response.universities)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(universities -> {
                this.items.clear();
                this.items.addAll(universities);
                this.adapter.notifyDataSetChanged();
            }, error -> {
                Timber.d("get university list error : %s", error);
                error.printStackTrace();
            })
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    public void onPageFocused() {
        FloatingActionControl.getInstance().setControl(R.layout.fab_next);
        if(SignUpForm.getInstance().getUniversityId() != null && SignUpForm.getInstance().getEntranceYear() != null) FloatingActionControl.getInstance().show(true);
        if(this.subscriptions.isUnsubscribed()) this.subscriptions = new CompositeSubscription();

        this.subscriptions.add(FloatingActionControl.clicks().subscribe(
            unused -> {
                InputMethodManager imm = ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                imm.showSoftInput(this.universityList, InputMethodManager.SHOW_FORCED);
                this.subscriptions.add(
                    Observable
                        .timer(300, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                            unuse -> this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP2, true),
                            error -> error.printStackTrace()
                        )
                );
            },
            error -> Timber.d("page change error %s", error)
        ));

        InputMethodManager imm = ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
        imm.hideSoftInputFromWindow(this.universityList.getWindowToken(), 0);
    }

    @Override
    public void onPageUnfocused() {
        if(this.subscriptions !=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    protected UniversityAdapter getAdapter () {
        return new UniversityAdapter(this.items, this);
    }

    @Override
    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new GridLayoutManager(this.getActivity(), 2);
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        for(int i = 0; i < this.universityList.getChildCount(); i ++) {
            this.universityList.getChildAt(i).setSelected(false);
        } view.setSelected(true);

        SignUpForm.getInstance().setUniversityId(this.items.get(position).id);
        SignUpForm.getInstance().setImageUrl(this.items.get(position).image_url);

        final int length = Calendar.getInstance().get(Calendar.YEAR) - AppConst.MIN_ENTRANCE_YEAR + 1;
        String[] years = new String[length];
        for(int i = 0; i < length; i ++) years[i] = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - i);
        new MaterialDialog.Builder(this.getActivity())
            .title(R.string.dialog_title_entrance_year)
            .negativeText(R.string.cancel)
            .buttonsGravity(GravityEnum.START)
            .items(years)
            .itemsCallback((dialog, v, which, text) -> {
                SignUpForm.getInstance().setEntranceYear(Integer.parseInt(text.toString()));
                InputMethodManager imm = ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                imm.showSoftInput(this.universityList, InputMethodManager.SHOW_FORCED);
                this.subscriptions.add(
                    Observable
                        .timer(300, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                            unuse -> this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP2, true),
                            error -> error.printStackTrace()
                        )
                );
            })
            .build()
            .show();
    }
}