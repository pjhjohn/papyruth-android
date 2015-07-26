package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.activity.AuthActivity;
import com.montserrat.app.activity.MainActivity;
import com.montserrat.app.model.unique.Signup;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.support.rx.RxValidator;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.OnPageUnfocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.montserrat.utils.support.rx.RxValidator.isValidRadioButton;
import static com.montserrat.utils.support.rx.RxValidator.toString;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep4Fragment extends Fragment implements OnPageFocus, OnPageUnfocus{
    private ViewPagerController pagerController;

    @InjectView(R.id.password) protected EditText password;

    private Boolean isNext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    private CompositeSubscription subscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step4, container, false);
        ButterKnife.inject(this, view);
        this.subscription = new CompositeSubscription();
        this.isNext = false;
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
        Timber.d(Signup.getInstance().toString());
        if(this.subscription.isUnsubscribed())
            this.subscription = new CompositeSubscription();
        ((AuthActivity)this.getActivity()).signUpStep(4);

        if(Signup.getInstance().getPassword() != null){
            this.password.setText(Signup.getInstance().getPassword());
            this.isNext = true;
        }

        FloatingActionControl.getInstance();
        this.subscription.add(
            WidgetObservable.text(this.password).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .map(toString)
                .map(RxValidator.getErrorMessagePassword)
                .map(
                    (String passwordError) -> {
                        this.password.setError(passwordError);
                        return passwordError == null;
                    })
                .subscribe(
                    valid -> {
                        boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                        Timber.d("%s %s", visible, valid);
                        if (visible && !valid) FloatingActionControl.getInstance().hide(true);
                        else if (isNext||(!visible && valid)) FloatingActionControl.getInstance().show(true);
                    }
                )
        );
        this.subscription.add(FloatingActionControl
                .clicks()
                .subscribe(unused -> {
                    Signup.getInstance().setPassword(this.password.getText().toString());
                    this.register();
                }, error -> Timber.d("page change error %s", error))
        );
    }

    private void register(){
        this.subscription.add(
            RetrofitApi.getInstance().users_sign_up(
                Signup.getInstance().getEmail(),
                Signup.getInstance().getPassword(),
                Signup.getInstance().getRealname(),
                Signup.getInstance().getNickname(),
                Signup.getInstance().getIs_boy(),
                Signup.getInstance().getUniversity_id(),
                Signup.getInstance().getEntrance_year()
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    if (response.success) {
                        User.getInstance().update(response.user, response.access_token);
                        AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, response.access_token);
                        Signup.getInstance().clear();
                        SignUpStep4Fragment.this.getActivity().startActivity(new Intent(SignUpStep4Fragment.this.getActivity(), MainActivity.class));
                        SignUpStep4Fragment.this.getActivity().finish();
                    } else {
                        Toast.makeText(this.getActivity(), this.getResources().getString(R.string.failed_sign_in), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    if (error instanceof RetrofitError) {
                        switch (((RetrofitError) error).getResponse().getStatus()) {
                            case 400: // Invalid field or lack of required field.
                            case 403: // Failed to SignUp
                                Toast.makeText(this.getActivity(), this.getResources().getString(R.string.failed_sign_up), Toast.LENGTH_LONG).show();
                                break;
                            default:
                                Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                        }
                    }
                }
            )
        );
    }

    @Override
    public void onPageUnfocused() {
        if(this.subscription !=null && !this.subscription.isUnsubscribed()) this.subscription.unsubscribe();
    }
}
