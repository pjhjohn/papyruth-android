package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.activity.AuthActivity;
import com.montserrat.app.activity.MainActivity;
import com.montserrat.app.model.error.SignupError;
import com.montserrat.app.model.unique.Signup;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.support.rx.RxValidator;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.OnPageUnfocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.RetrofitError;
import retrofit.mime.TypedByteArray;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep4Fragment extends Fragment implements OnPageFocus, OnPageUnfocus{
    private ViewPagerController pagerController;

    @InjectView(R.id.password) protected EditText password;
    @InjectView(R.id.icon_password) protected ImageView iconPassword;
    @InjectView(R.id.agree_term) protected TextView agreeTerm;
    @InjectView(R.id.term_agree) protected CheckBox termAgree;

    private MaterialDialog termPage;

    private CharSequence termContents;

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
        Picasso.with(this.getActivity().getBaseContext()).load(R.drawable.ic_light_lock).transform(new ColorFilterTransformation(this.getResources().getColor(R.color.primary_dark_material_dark))).into(this.iconPassword);

        this.subscription.add(
            RetrofitApi.getInstance().terms(0)
                .map(terms -> terms.term)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    term -> {
                        if (term != null && term.size() > 0) {
                            this.termContents = term.get(0).body;
                        }else {
                            this.termContents = "";
                        }
                    }, error -> {
                        Timber.d("get Term error", error);
                        error.printStackTrace();
                    }
                )
        );
    }

    public void showFAC() {
        String validatePassword = RxValidator.getErrorMessagePassword.call(this.password.getText().toString());

        boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
        boolean valid = validatePassword == null;

        if (!visible && valid) {
            FloatingActionControl.getInstance().show(true);
        }else if (visible && !valid) {
            FloatingActionControl.getInstance().hide(true);
        }
    }

    private void buildTermDialog(){
        this.termContents = this.getResources().getString(R.string.lorem_ipsum);
        this.termPage = new MaterialDialog.Builder(this.getActivity())
            .title(R.string.term)
            .content(this.termContents)
            .positiveText(R.string.agree_terms)
            .build();
    }

    @Override
    public void onPageFocused() {
        ((AuthActivity)this.getActivity()).signUpStep(4);
        FloatingActionControl.getInstance().hide(true);

        if(this.subscription.isUnsubscribed())
            this.subscription = new CompositeSubscription();

        if(Signup.getInstance().getPassword() != null){
            this.password.setText(Signup.getInstance().getPassword());
            this.showFAC();
        }
        this.subscription.add(
            WidgetObservable
                .text(this.password)
                .debounce(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    String validatePassword = RxValidator.getErrorMessagePassword.call(event.text().toString());
                    this.password.setError(validatePassword);
                    this.showFAC();
                })
        );

        this.subscription.add(FloatingActionControl
                .clicks()
                .subscribe(unused -> {
                    Signup.getInstance().setPassword(this.password.getText().toString());
                    this.register();
                }, error -> Timber.d("page change error %s", error))
        );
        this.subscription.add(
            ViewObservable.clicks(this.agreeTerm)
                .filter(unuse -> !this.termPage.isShowing())
                .subscribe(
                unused -> {
//                    this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_TERM, true)
                    this.termPage.show();
                })
        );

        this.buildTermDialog();
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
                                String json = new String(((TypedByteArray)((RetrofitError)error).getResponse().getBody()).getBytes());
                                Gson gson = new Gson();
                                Timber.d("reason : %s", gson.fromJson(json, SignupError.class).errors.email);
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