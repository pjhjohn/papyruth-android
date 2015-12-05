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
import com.papyruth.android.PapyruthApplication;
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.model.unique.SignUpForm;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.opensource.rx.RxValidator;
import com.papyruth.support.utility.navigator.NavigatableLinearLayout;
import com.papyruth.support.utility.navigator.Navigator;
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
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep3Fragment extends Fragment {
    private AuthActivity mActivity;
    private com.papyruth.support.utility.navigator.Navigator mNavigator;
    private Tracker mTracker;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AuthActivity) activity;
        mNavigator = (com.papyruth.support.utility.navigator.Navigator) activity;
        mTracker = ((PapyruthApplication) mActivity.getApplication()).getTracker();
    }

    @InjectView(R.id.signup_step3_container) protected NavigatableLinearLayout mContainer;
    @InjectView(R.id.signup_gender_radiogroup)        protected RadioGroup mRadioGroupGender;
    @InjectView(R.id.signup_realname_text)      protected EditText mTextRealname;
    @InjectView(R.id.signup_gender_icon)   protected ImageView mIconGender;
    @InjectView(R.id.signup_realname_icon) protected ImageView mIconRealname;
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
        mContainer.setOnBackListner(() -> {
            this.mNavigator.back();
            return true;
        });
        Picasso.with(mActivity).load(R.drawable.ic_light_gender).transform(new ColorFilterTransformation(getResources().getColor(R.color.icon_material))).into(mIconGender);
        Picasso.with(mActivity).load(R.drawable.ic_light_realname).transform(new ColorFilterTransformation(getResources().getColor(R.color.icon_material))).into(mIconRealname);
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_auth_signup3));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mActivity.setCurrentAuthStep(AppConst.Navigator.Auth.SIGNUP_STEP3);
        final View focusedView = mActivity.getWindow().getCurrentFocus();
        Observable.timer(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(
            unused -> ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(focusedView != null ? focusedView : mTextRealname, InputMethodManager.SHOW_FORCED)
        );

        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_next).hide(true);

        if(mTextRealname.getText().toString().isEmpty()) {
            final String realname = SignUpForm.getInstance().getRealname();
            if(realname != null) mTextRealname.setText(realname);
            else mTextRealname.getText().clear();
        } else mTextRealname.setText(mTextRealname.getText());
        if(mRadioGroupGender.getCheckedRadioButtonId() < 0) {
            final Boolean isboy = SignUpForm.getInstance().getIsBoy();
            if(isboy != null) mRadioGroupGender.check(isboy? R.id.signup_gender_radio_male : R.id.signup_gender_radio_female);
        } else mRadioGroupGender.check(mRadioGroupGender.getCheckedRadioButtonId());

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
                            }
                        }
                    }
                    return realnameError == null && (checkedId == R.id.signup_gender_radio_male || checkedId == R.id.signup_gender_radio_female);
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
                    if (checkedGenderRadioButtonId >= 0) SignUpForm.getInstance().setIsBoy(checkedGenderRadioButtonId == R.id.signup_gender_radio_male);
                    mNavigator.navigate(SignUpStep4Fragment.class, true);
                }
            }
        ));

        Observable.timer(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(unused -> {
            if(mTextRealname != null) mTextRealname.requestFocus();
            ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mTextRealname, InputMethodManager.SHOW_FORCED);
        });
    }

    private boolean mNextButtonEnabled;
    private Observable<String> getRealnameValidationObservable(TextView realnameTextView) {
        Observable<String> observable =  WidgetObservable.text(realnameTextView)
            .map(event -> {
                mNextButtonEnabled = false;
                return event;
            })
            .map(event -> event.text().toString())
            .map(RxValidator.getErrorMessageRealname)
            .observeOn(AndroidSchedulers.mainThread());

        if(SignUpForm.getInstance().getRealname() != null)
            observable =  observable.startWith(RxValidator.getErrorMessageRealname.call(realnameTextView.getText().toString()));

        return observable;
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
        return Observable.from(buttons).flatMap(ViewObservable::clicks).map(event -> event.view().getId()).startWith(mRadioGroupGender.getCheckedRadioButtonId());
    }
}
