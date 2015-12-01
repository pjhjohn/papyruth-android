package com.papyruth.android.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.model.unique.SignUpForm;
import com.papyruth.android.papyruth;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.opensource.rx.RxValidator;
import com.papyruth.support.utility.viewpager.OnPageFocus;
import com.papyruth.support.utility.viewpager.OnPageUnfocus;
import com.papyruth.support.utility.viewpager.ViewPagerController;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.android.widget.WidgetObservable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep3Fragment extends Fragment implements OnPageFocus, OnPageUnfocus {
    private ViewPagerController mViewPagerController;
    private Context mContext;
    private Tracker mTracker;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mViewPagerController = ((AuthActivity) activity).getViewPagerController();
        mContext = activity;
        mTracker = ((papyruth) getActivity().getApplication()).getTracker();
    }

    @InjectView(R.id.gender)        protected RadioGroup mRadioGroupGender;
    @InjectView(R.id.realname)      protected EditText mTextRealname;
    @InjectView(R.id.icon_gender)   protected ImageView mIconGender;
    @InjectView(R.id.icon_realname) protected ImageView mIconRealname;
    private CompositeSubscription mCompositeSubscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step3, container, false);
        ButterKnife.inject(this, view);
        mCompositeSubscription = new CompositeSubscription();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        Picasso.with(mContext).load(R.drawable.ic_light_gender).transform(new ColorFilterTransformation(getResources().getColor(R.color.icon_material))).into(mIconGender);
        Picasso.with(mContext).load(R.drawable.ic_light_realname).transform(new ColorFilterTransformation(getResources().getColor(R.color.icon_material))).into(mIconRealname);
        mViewPagerController.addImeControlFragment(AppConst.ViewPager.Auth.SIGNUP_STEP3);
        if(mViewPagerController.getCurrentPage() == AppConst.ViewPager.Auth.SIGNUP_STEP3) {
            final View focusedView = getActivity().getWindow().getCurrentFocus();
            Observable.timer(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(
                unused -> ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(focusedView != null ? focusedView : mTextRealname, InputMethodManager.SHOW_FORCED)
            );
        }
    }

    private boolean mNextButtonEnabled;
    private Observable<String> getRealnameValidationObservable(TextView realnameTextView) {
        return WidgetObservable.text(realnameTextView)
            .map(event -> {
                mNextButtonEnabled = false;
                return event;
            })
            .map(event -> event.text().toString())
            .map(RxValidator.getErrorMessageRealname)
            .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<Integer> getGenderValidationObservable(RadioGroup group) {
        List<RadioButton> buttons = new ArrayList<>();
        Queue<ViewGroup> queue = new LinkedList<>();
        queue.add(group);
        while (!queue.isEmpty()) {
            ViewGroup head = queue.remove();
            for (int i = 0; i < head.getChildCount(); i++) {
                View child = head.getChildAt(i);
                if (child instanceof ViewGroup) {
                    queue.add((ViewGroup) child);
                } else if (child instanceof RadioButton) {
                    buttons.add((RadioButton) child);
                }
            }
        }
        return Observable.from(buttons).flatMap(ViewObservable::clicks).map(event -> event.view().getId());
    }

    @Override
    public void onPageFocused() {
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_auth_signup3));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_next).hide(true);
        ((AuthActivity) getActivity()).setOnShowSoftKeyboard(null);
        ((AuthActivity) getActivity()).setOnHideSoftKeyboard(null);
        if(mCompositeSubscription.isUnsubscribed()) mCompositeSubscription = new CompositeSubscription();

        mCompositeSubscription.add(
            Observable.combineLatest(
                getRealnameValidationObservable(mTextRealname),
                getGenderValidationObservable(mRadioGroupGender),
                (String realnameError, Integer checkedId) -> {
                    mTextRealname.setError(realnameError);
                    Queue<ViewGroup> queue = new LinkedList<>();
                    queue.add(mRadioGroupGender);
                    while (!queue.isEmpty()) {
                        ViewGroup head = queue.remove();
                        for (int i = 0; i < head.getChildCount(); i++) {
                            View child = head.getChildAt(i);
                            if (child instanceof ViewGroup) {
                                queue.add((ViewGroup) child);
                            } else if (child instanceof RadioButton) {
                                ((RadioButton) child).setError(checkedId < 0? mContext.getString(R.string.field_invalid_gender) : null);
                            }
                        }
                    }
                    return realnameError == null && (checkedId == R.id.gender_male || checkedId == R.id.gender_female);
                }
            ).observeOn(AndroidSchedulers.mainThread()).subscribe(valid -> {
                if (valid) mNextButtonEnabled = true;
                boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                if (!visible && valid) FloatingActionControl.getInstance().show(true);
                else if (visible && !valid) FloatingActionControl.getInstance().hide(true);
            }, Throwable::printStackTrace)
        );

        mCompositeSubscription.add(FloatingActionControl.clicks().subscribe(
            unused -> {
                if (mNextButtonEnabled) {
                    SignUpForm.getInstance().setRealname(mTextRealname.getText().toString());
                    final int checkedGenderRadioButtonId = mRadioGroupGender.getCheckedRadioButtonId();
                    if (checkedGenderRadioButtonId >= 0) SignUpForm.getInstance().setIsBoy(checkedGenderRadioButtonId == R.id.gender_male);
                    mViewPagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP4, true);
                }
            }
        ));

        if(mTextRealname.getText().toString().isEmpty()) {
            final String realname = SignUpForm.getInstance().getRealname();
            if(realname != null) mTextRealname.setText(realname);
            else mTextRealname.getText().clear();
        } else mTextRealname.setText(mTextRealname.getText());
        if(mRadioGroupGender.getCheckedRadioButtonId() < 0) {
            final Boolean isboy = SignUpForm.getInstance().getIsBoy();
            if(isboy != null) mRadioGroupGender.check(isboy? R.id.gender_male : R.id.gender_female);
        } else mRadioGroupGender.check(mRadioGroupGender.getCheckedRadioButtonId());

        Observable.timer(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(unused -> {
            mTextRealname.requestFocus();
            ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mTextRealname, InputMethodManager.SHOW_FORCED);
        });
    }

    @Override
    public void onPageUnfocused() {
        if(mCompositeSubscription ==null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }
}
