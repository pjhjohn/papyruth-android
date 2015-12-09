package com.papyruth.android.fragment.auth;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.model.unique.SignUpForm;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.opensource.rx.RxValidator;
import com.papyruth.support.utility.fragment.TrackerFragment;
import com.papyruth.support.utility.navigator.NavigatableLinearLayout;
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

public class SignUpStep3Fragment extends TrackerFragment {
    private AuthActivity mActivity;
    private com.papyruth.support.utility.navigator.Navigator mNavigator;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AuthActivity) activity;
        mNavigator = (com.papyruth.support.utility.navigator.Navigator) activity;
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
        mActivity.setCurrentAuthStep(AppConst.Navigator.Auth.SIGNUP_STEP3);
        final View focusedView = mActivity.getWindow().getCurrentFocus();
        Observable.timer(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(
            unused -> ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(focusedView != null ? focusedView : mTextRealname, InputMethodManager.SHOW_FORCED)
        );

        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_next).hide(true);

        if(mTextRealname.getText().toString().isEmpty()) {
            final String realname = SignUpForm.getInstance().getTempSaveRealname();
            if(realname != null) mTextRealname.setText(realname);
            else mTextRealname.getText().clear();
        } else mTextRealname.setText(mTextRealname.getText());
        if(mRadioGroupGender.getCheckedRadioButtonId() < 0) {
            final Boolean isboy = SignUpForm.getInstance().getTempSaveIsBoy();
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

        mTextRealname.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_NEXT) {
                proceedNextStep();
                return true;
            }
            return false;
        });

        mCompositeSubscription.add(FloatingActionControl.clicks().subscribe(
            unused -> {
                proceedNextStep();
            }
        ));

        Observable.timer(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(unused -> {
            if(mTextRealname != null) mTextRealname.requestFocus();
            ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mTextRealname, InputMethodManager.SHOW_FORCED);
        });
    }

    private void proceedNextStep(){
        if (mNextButtonEnabled) {
            SignUpForm.getInstance().setValidRealname();
            SignUpForm.getInstance().setValidIsBoy();
            mNavigator.navigate(SignUpStep4Fragment.class, true);
        }
    }

    private boolean mNextButtonEnabled;
    private Observable<String> getRealnameValidationObservable(TextView realnameTextView) {
        Observable<String> observable =  WidgetObservable.text(realnameTextView)
            .map(event -> {
                mNextButtonEnabled = false;
                return event;
            })
            .map(event -> event.text().toString())
            .map(realname -> {
                SignUpForm.getInstance().setTempSaveRealname(realname);
                return RxValidator.getErrorMessageRealname.call(realname);
            })
            .observeOn(AndroidSchedulers.mainThread());

        if(SignUpForm.getInstance().getTempSaveRealname() != null)
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
        return Observable.from(buttons).flatMap(ViewObservable::clicks).map(event -> {
            if(event.view().getId() > 0) SignUpForm.getInstance().setTempSaveIsBoy(event.view().getId() == R.id.signup_gender_radio_male);
            else SignUpForm.getInstance().setTempSaveIsBoy(null);
            return event.view().getId();
        }).startWith(mRadioGroupGender.getCheckedRadioButtonId());
    }
}
