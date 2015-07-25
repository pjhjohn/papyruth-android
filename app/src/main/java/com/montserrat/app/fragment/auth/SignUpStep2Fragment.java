package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.activity.AuthActivity;
import com.montserrat.app.model.unique.Signup;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.rx.RxValidator;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.OnPageUnfocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;
import com.rengwuxian.materialedittext.MaterialEditText;


import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.android.widget.WidgetObservable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.montserrat.utils.support.rx.RxValidator.toString;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep2Fragment extends Fragment implements OnPageFocus, OnPageUnfocus{
    private ViewPagerController pagerController;
    @InjectView(R.id.email) protected MaterialEditText email;
    @InjectView(R.id.nickname) protected MaterialEditText nickname;
    @InjectView(R.id.nextBtn) protected Button next;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    private CompositeSubscription subscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step2, container, false);
        ButterKnife.inject(this, view);
        this.subscription = new CompositeSubscription();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscription !=null && !this.subscription.isUnsubscribed()) this.subscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPageFocused() {
        ((AuthActivity)this.getActivity()).signUpStep(2);

        if(this.subscription.isUnsubscribed())
            this.subscription = new CompositeSubscription();

        FloatingActionControl.getInstance().setControl(R.layout.fab_next);
        this.subscription.add(
            Observable.combineLatest(
                WidgetObservable.text(this.email).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessageEmail),
                WidgetObservable.text(this.nickname).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessageNickname),
                (String emailError, String nicknameError) -> {
                    this.email.setError(emailError);
                    this.nickname.setError(nicknameError);
                    return emailError == null && nicknameError == null;
                })
            .startWith(false)
            .subscribe(
                valid -> {
                    boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                    if (visible && !valid) FloatingActionControl.getInstance().hide(true);
                    else if (!visible && valid) FloatingActionControl.getInstance().show(true);
                }
            )
        );
        this.subscription.add(
            ViewObservable
                .clicks(FloatingActionControl.getButton())
                .subscribe(unused -> {
                    Signup.getInstance().setEmail(this.email.getText().toString());
                    Signup.getInstance().setNickname(this.nickname.getText().toString());
//                    if (this.pagerController.getPreviousPage() == AppConst.ViewPager.Auth.SIGNUP_STEP3) {
//                        if (this.pagerController.getHistoryCopy().contains(AppConst.ViewPager.Auth.SIGNUP_STEP2)) this.pagerController.popCurrentPage();
//                        else this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP3, true);
//                    } else this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP3, true);
                    this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP3, true);
                }, error -> Timber.d("page change error %s", error))
        );
//        this.next.setOnClickListener(v -> {
//            this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP3, true);
//        });
        this.subscription.add(
            ViewObservable.clicks(this.next)
            .subscribe(u -> {
                this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP3, true);
            })
        );
    }

    @Override
    public void onPageUnfocused() {
        if(this.subscription !=null && !this.subscription.isUnsubscribed()) this.subscription.unsubscribe();
    }
}
