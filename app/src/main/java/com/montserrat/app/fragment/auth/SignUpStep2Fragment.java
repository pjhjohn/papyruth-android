package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.activity.AuthActivity;
import com.montserrat.app.model.unique.Signup;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.support.rx.RxValidator;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.OnPageUnfocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.android.widget.WidgetObservable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep2Fragment extends Fragment implements OnPageFocus, OnPageUnfocus{
    private ViewPagerController pagerController;
    @InjectView(R.id.email) protected EditText email;
    @InjectView(R.id.nickname) protected EditText nickname;
    @InjectView(R.id.icon_email) protected ImageView iconEmail;
    @InjectView(R.id.icon_nickname) protected ImageView iconNickname;
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
        Picasso.with(this.getActivity().getBaseContext()).load(R.drawable.ic_light_mail).transform(new ColorFilterTransformation(this.getResources().getColor(R.color.primary_dark_material_dark))).into(this.iconEmail);
        Picasso.with(this.getActivity().getBaseContext()).load(R.drawable.ic_light_person).transform(new ColorFilterTransformation(this.getResources().getColor(R.color.primary_dark_material_dark))).into(this.iconNickname);
    }

    private boolean isDuplicateEmail = false;
    private boolean isDuplicateNickname = false;

    private String duplicatedValidator(String email, String nickcname){
        String errorMsg = null;
        this.subscription.add(
            RetrofitApi.getInstance().validate((email != null ? AppConst.Preference.EMAIL : AppConst.Preference.NICKNAME), (email != null ? email : nickcname))
                .map(validator -> validator.validation)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    success -> {
                        if(success) {
                            if (email != null) isDuplicateEmail = true;
                            else isDuplicateNickname = true;
                        }else{
                            if(email != null) {
                                this.email.setError(getResources().getString(R.string.duplicated_email));
                            }else {
                                this.nickname.setError(getResources().getString(R.string.duplicated_nickname));
                            }
                        }
                        this.showFAC();
                    },
                    error -> {
                        Timber.d("duplicate validator error : %s", error);
                        error.printStackTrace();
                    }
                )
        );
        return errorMsg;
    }

    public void showFAC() {
        String validateNickname = RxValidator.getErrorMessageNickname.call(this.nickname.getText().toString());
        String validateEmail = RxValidator.getErrorMessageEmail.call(this.email.getText().toString());


        boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
        boolean valid = validateEmail == null && validateNickname == null && isDuplicateEmail && isDuplicateNickname;
        Timber.d("vali : %s %s", visible, valid);
        if (!visible && valid) {
            FloatingActionControl.getInstance().show(true);
        }else if (visible && !valid) {
            FloatingActionControl.getInstance().hide(true);
        }
    }

    @Override
    public void onPageFocused() {
        ((AuthActivity)this.getActivity()).signUpStep(2);
        FloatingActionControl.getInstance().setControl(R.layout.fab_next);

        if(this.subscription.isUnsubscribed())
            this.subscription = new CompositeSubscription();

        if(Signup.getInstance().getNickname() != null){
            this.email.setText(Signup.getInstance().getEmail());
            this.nickname.setText(Signup.getInstance().getNickname());
            this.duplicatedValidator(email.getText().toString(), null);
            this.duplicatedValidator(null, nickname.getText().toString());
            this.showFAC();
        }else if(this.email.length() > 0 && this.nickname.length() > 0){
            this.duplicatedValidator(email.getText().toString(), null);
            this.duplicatedValidator(null, nickname.getText().toString());
        }


        this.subscription.add(
            Observable.merge(
                WidgetObservable
                    .text(this.email)
                    .debounce(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                    .doOnNext(event -> {
                        isDuplicateEmail = false;
                        this.duplicatedValidator(event.text().toString(), null);
                        this.email.setError(RxValidator.getErrorMessageEmail.call(event.text().toString()));
                    }),
                WidgetObservable
                    .text(this.nickname)
                    .debounce(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                    .doOnNext(event -> {
                        isDuplicateNickname = false;
                        this.duplicatedValidator(null, event.text().toString());
                        this.nickname.setError(RxValidator.getErrorMessageNickname.call(event.text().toString()));
                    })
            ).subscribe(event -> {
                Timber.d("event class", event.getClass());
                Timber.d("error msg : %s >< %s", this.email.getError(), this.nickname.getError());
                this.showFAC();
            })
        );

        this.subscription.add(
            ViewObservable
                .clicks(FloatingActionControl.getButton())
                .subscribe(unused -> {
                    Signup.getInstance().setEmail(this.email.getText().toString());
                    Signup.getInstance().setNickname(this.nickname.getText().toString());
                    this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP3, true);
                }, error -> Timber.d("page change error %s", error))
        );
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
