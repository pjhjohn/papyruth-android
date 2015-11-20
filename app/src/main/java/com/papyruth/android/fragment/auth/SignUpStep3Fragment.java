package com.papyruth.android.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
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
import com.papyruth.utils.support.error.ErrorHandler;
import com.papyruth.utils.support.fab.FloatingActionControl;
import com.papyruth.utils.support.picasso.ColorFilterTransformation;
import com.papyruth.utils.support.rx.RxValidator;
import com.papyruth.utils.view.viewpager.OnPageFocus;
import com.papyruth.utils.view.viewpager.OnPageUnfocus;
import com.papyruth.utils.view.viewpager.ViewPagerController;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
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
        Picasso.with(mContext).load(R.drawable.ic_light_person).transform(new ColorFilterTransformation(getResources().getColor(R.color.icon_material))).into(mIconRealname);
        mViewPagerController.addImeControlFragment(AppConst.ViewPager.Auth.SIGNUP_STEP3);
        if(mViewPagerController.getCurrentPage() == AppConst.ViewPager.Auth.SIGNUP_STEP3){
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mTextRealname, InputMethodManager.SHOW_FORCED);
        }
    }

    public void showFAC() {
        String validateName = RxValidator.getErrorMessageRealname.call(mTextRealname.getText().toString());

        boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
        boolean valid = validateName == null && mRadioGroupGender.getCheckedRadioButtonId() != -1;

        if (!visible && valid) FloatingActionControl.getInstance().show(true, 200, TimeUnit.MILLISECONDS);
        else if (visible && !valid) FloatingActionControl.getInstance().hide(true);
    }

    @Override
    public void onPageFocused() {
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_next).hide(true);
        ((AuthActivity) getActivity()).setOnShowSoftKeyboard(null);
        ((AuthActivity) getActivity()).setOnHideSoftKeyboard(null);
        if(mCompositeSubscription.isUnsubscribed()) mCompositeSubscription = new CompositeSubscription();

        InputMethodManager imm = ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        imm.showSoftInput(mTextRealname, InputMethodManager.SHOW_FORCED);
        mTextRealname.requestFocus();

        if(SignUpForm.getInstance().getRealname() != null){
            mTextRealname.setText(SignUpForm.getInstance().getRealname());
            ((RadioButton) mRadioGroupGender.findViewById(mRadioGroupGender.getChildAt((SignUpForm.getInstance().getIsBoy()?0:1)).getId())).setChecked(true);
            showFAC();
        }

        if(mTextRealname.length() > 0 && mRadioGroupGender.getCheckedRadioButtonId() != -1){
            showFAC();
        }

        mCompositeSubscription.add(WidgetObservable
            .text(mTextRealname)
            .debounce(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe(event -> {
                String validateName = RxValidator.getErrorMessageRealname.call(event.text().toString());
                mTextRealname.setError(validateName);
                showFAC();
            }, error -> ErrorHandler.throwError(error, this))
        );

        mRadioGroupGender.setOnCheckedChangeListener((group, id) -> showFAC());

        mCompositeSubscription.add(Observable
            .mergeDelayError(
                FloatingActionControl.clicks().map(event -> FloatingActionControl.getButton().getVisibility() == View.VISIBLE),
                Observable.create(observer -> mTextRealname.setOnEditorActionListener((TextView v, int action, KeyEvent event) -> {
                    observer.onNext(FloatingActionControl.getButton().getVisibility() == View.VISIBLE);
                    return !(FloatingActionControl.getButton().getVisibility() == View.VISIBLE);
                }))
            )
            .filter(use -> use)
            .subscribe(
                use -> {
                    SignUpForm.getInstance().setRealname(mTextRealname.getText().toString());
                    SignUpForm.getInstance().setIsBoy(((RadioButton) mRadioGroupGender.findViewById(mRadioGroupGender.getCheckedRadioButtonId())).getText().equals(getResources().getString(R.string.gender_male)));
                    mViewPagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP4, true);
                }, error -> ErrorHandler.throwError(error, this)
            )
        );
    }

    @Override
    public void onPageUnfocused() {
        if(mCompositeSubscription ==null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }
}
