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
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.model.UniversityData;
import com.papyruth.android.model.unique.SignUpForm;
import com.papyruth.android.recyclerview.adapter.UniversityAdapter;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.error.ErrorNetwork;
import com.papyruth.support.utility.fragment.TrackerFragment;
import com.papyruth.support.utility.navigator.NavigatableFrameLayout;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;


/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep1Fragment extends TrackerFragment implements RecyclerViewItemClickListener {
    private AuthActivity mActivity;
    private Navigator mNavigator;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AuthActivity) activity;
        mNavigator = (Navigator) activity;
    }

    @Bind(R.id.signup_university_recyclerview) protected RecyclerView mUniversityRecyclerView;
    @Bind(R.id.signup_step1_container) protected NavigatableFrameLayout mContainer;
    private CompositeSubscription mCompositeSubscription;
    private List<UniversityData> mUniversities;
    private UniversityAdapter mAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step1, container, false);
        ButterKnife.bind(this, view);
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
        ButterKnife.unbind(this);
        if(mCompositeSubscription ==null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCompositeSubscription.clear();
        mActivity.setCurrentAuthStep(AppConst.Navigator.Auth.SIGNUP_STEP1);
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_next);
        if(SignUpForm.getInstance().getUniversityId() != null && SignUpForm.getInstance().getEntranceYear() != null) FloatingActionControl.getInstance().show(true);
        Observable.timer(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(unused -> {
            if(mUniversityRecyclerView != null)
                ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mUniversityRecyclerView.getWindowToken(), 0);
        });

        mCompositeSubscription.add(FloatingActionControl.clicks().subscribe(unused -> mNavigator.navigate(SignUpStep2Fragment.class, true)));

        if(SignUpForm.getInstance().getUniversityList().size() < 1) {
            Api.papyruth()
                .get_universities()
                .map(response -> response.universities)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(universities -> {
                    notifyUniversityChanged(universities);
                    SignUpForm.getInstance().setUniversityList(universities);
                }, error -> ErrorHandler.handle(error, this));
        }else{
            notifyUniversityChanged(SignUpForm.getInstance().getUniversityList());
        }

    }

    private void notifyUniversityChanged(List<UniversityData> universities) {
        mUniversities.clear();
        mUniversities.addAll(universities);
        mAdapter.notifyDataSetChanged();
        if(SignUpForm.getInstance().getUniversityId() != null) mAdapter.setSelected(selectedUniversityItem(universities, SignUpForm.getInstance().getUniversityId()));
        else if(mUniversities.size() == 1) Observable.timer(200, TimeUnit.MILLISECONDS, Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(unused -> {
            if(mUniversityRecyclerView.getChildAt(0) != null) mUniversityRecyclerView.getChildAt(0).performClick();
        });
    }

    private int selectedUniversityItem(List<UniversityData> universities, int id) {
        for(int i = 0; i < universities.size(); i++) {
            if(universities.get(i).id == id) return i;
        } return -1;
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        for(int i = 0; i < mUniversityRecyclerView.getChildCount(); i ++) mUniversityRecyclerView.getChildAt(i).setSelected(false);
        Api.papyruth().get_global_infos().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(
            response -> {
                view.setSelected(true);
                SignUpForm.getInstance().setUniversityId(mUniversities.get(position).id);
                SignUpForm.getInstance().setImageUrl(mUniversities.get(position).image_url);
                final int length = Calendar.getInstance().get(Calendar.YEAR) - response.global_infos.get(0).start_of_entrance_year + 1;
                String[] years = new String[length];
                for (int i = 0; i < length; i++)
                    years[i] = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - i);
                new MaterialDialog.Builder(mActivity)
                    .title(R.string.dialog_title_entrance_year)
                    .negativeText(R.string.dialog_negative_cancel)
                    .buttonsGravity(GravityEnum.START)
                    .items(years)
                    .itemsCallback((dialog, v, which, text) -> {
                        SignUpForm.getInstance().setEntranceYear(Integer.parseInt(text.toString()));
                        mNavigator.navigate(SignUpStep2Fragment.class, true);
                    })
                    .show();
            },
            error -> {
                boolean handled = false;
                if(error instanceof RetrofitError) handled = ErrorNetwork.handle((RetrofitError) error, this).handled;
                if(handled) Toast.makeText(mActivity, R.string.toast_error_retrofit_unstable_network, Toast.LENGTH_SHORT).show();
                else Toast.makeText(mActivity, R.string.toast_signup_min_entrance_year_not_loaded, Toast.LENGTH_SHORT).show();
            }
        );
    }
}