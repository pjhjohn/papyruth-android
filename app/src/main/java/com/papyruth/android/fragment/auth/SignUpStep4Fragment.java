package com.papyruth.android.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.PapyruthApplication;
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.model.TermData;
import com.papyruth.android.model.error.SignupError;
import com.papyruth.android.model.unique.SignUpForm;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.opensource.rx.RxValidator;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.navigator.NavigatableLinearLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.RetrofitError;
import retrofit.mime.TypedByteArray;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep4Fragment extends Fragment {
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

    @InjectView(R.id.signup_step4_container) protected NavigatableLinearLayout mContainer;
    @InjectView(R.id.signup_password_text)      protected EditText mTextPassword;
    @InjectView(R.id.signup_password_icon) protected ImageView mIconPassword;
    @InjectView(R.id.signup_term)    protected TextView mTextAgreement;
    private List<CharSequence> mTermsOfServiceStringArguments;
    private CompositeSubscription mCompositeSubscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step4, container, false);
        ButterKnife.inject(this, view);
        mCompositeSubscription = new CompositeSubscription();
        mTermsOfServiceStringArguments = new ArrayList<>();
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
        Picasso.with(mActivity).load(R.drawable.ic_light_password).transform(new ColorFilterTransformation(mActivity.getResources().getColor(R.color.icon_material))).into(mIconPassword);
        InputMethodManager imm = ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE));
        final View focusedView = mActivity.getWindow().getCurrentFocus();
        Observable.timer(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(
            unused -> ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(focusedView != null ? focusedView : mTextPassword, InputMethodManager.SHOW_FORCED)
        );
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_auth_signup4));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mActivity.setCurrentAuthStep(AppConst.Navigator.Auth.SIGNUP_STEP4);
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_done_green).hide(true);
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        Api.papyruth().terms(0)
            .map(terms -> terms.term)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                term -> {
                    if (term == null || term.isEmpty()) return;
                    for (TermData data : term) mTermsOfServiceStringArguments.add(data.body);
                }, error -> ErrorHandler.handle(error, this)
            );

        String strTermsOfUse = getString(R.string.terms_of_use);
        String strPrivacyPolicy = getString(R.string.privacy_policy);
        String strAgreement = String.format(getString(R.string.agree_terms), strTermsOfUse, strPrivacyPolicy);
        SpannableString ss = new SpannableString(strAgreement);

        int index;
        index = strAgreement.indexOf(strTermsOfUse);
        ss.setSpan(
            new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    imm.hideSoftInputFromWindow(mTextPassword.getWindowToken(), 0);
                    new MaterialDialog.Builder(mActivity)
                        .title(R.string.terms_of_use)
                        .content(mTermsOfServiceStringArguments.size() > 0 ? mTermsOfServiceStringArguments.get(0) : getString(R.string.lorem_ipsum))
                        .positiveText(R.string.common_close)
                        .show();
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(true);
                }
            },
            index,
            index + strTermsOfUse.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        index = strAgreement.indexOf(strPrivacyPolicy);
        ss.setSpan(
            new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    new MaterialDialog.Builder(mActivity)
                        .title(R.string.privacy_policy)
                        .content(mTermsOfServiceStringArguments.size() > 1 ? mTermsOfServiceStringArguments.get(1) : getString(R.string.lorem_ipsum))
                        .positiveText(R.string.common_close)
                        .show();
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(true);
                }
            },
            index,
            index + strPrivacyPolicy.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        mTextAgreement.setText(ss);
        mTextAgreement.setMovementMethod(LinkMovementMethod.getInstance());

        mCompositeSubscription.add(
            getPasswordValidationObservable(mTextPassword)
                .map(passwordError -> {
                    mTextPassword.setError(passwordError);
                    return passwordError == null;
                })
                .observeOn(AndroidSchedulers.mainThread()).subscribe(valid -> {
                if (valid) mSubmitButtonEnabled = true;
                boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                if (!visible && valid) FloatingActionControl.getInstance().show(true);
                else if (visible && !valid) FloatingActionControl.getInstance().hide(true);
            }, Throwable::printStackTrace)
        );

        mCompositeSubscription.add(FloatingActionControl.clicks().subscribe(
            unused -> {
                proceedSubmit();
            }
        ));

        mTextPassword.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_NEXT) {
                proceedSubmit();
                return true;
            }
            return false;
        });

        if(mTextPassword.getText().toString().isEmpty()) {
            final String password = SignUpForm.getInstance().getTempSavePassword();
            if(password != null) mTextPassword.setText(password);
            else mTextPassword.getText().clear();
        } else mTextPassword.setText(mTextPassword.getText());

        Observable.timer(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(unused -> {
            if(mTextPassword != null) mTextPassword.requestFocus();
            ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mTextPassword, InputMethodManager.SHOW_FORCED);
        });
    }

    private void proceedSubmit(){
        if (mSubmitButtonEnabled) {
            SignUpForm.getInstance().setValidPassword();
            submitSignUpForm();
        }
    }

    private boolean mSubmitButtonEnabled;
    private Observable<String> getPasswordValidationObservable(TextView passwordTextView) {
        return WidgetObservable.text(passwordTextView)
            .map(event -> {
                mSubmitButtonEnabled = false;
                return event;
            })
            .map(event -> event.text().toString())
            .map(password -> {
                SignUpForm.getInstance().setTempSavePassword(password);
                return RxValidator.getErrorMessagePassword.call(password);
            })
            .observeOn(AndroidSchedulers.mainThread());
    }

    private void submitSignUpForm() {
        if(validateSignUpForm()) {
            Api.papyruth().users_sign_up(
                SignUpForm.getInstance().getValidEmail(),
                SignUpForm.getInstance().getValidPassword(),
                SignUpForm.getInstance().getValidRealname(),
                SignUpForm.getInstance().getValidNickname(),
                SignUpForm.getInstance().getValidIsBoy(),
                SignUpForm.getInstance().getUniversityId(),
                SignUpForm.getInstance().getEntranceYear()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        if (response.success) {
                            User.getInstance().update(response.user, response.access_token);
                            AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, response.access_token);
                            SignUpForm.getInstance().clear();
                            mActivity.startMainActivity();
                        } else
                            Toast.makeText(mActivity, getResources().getString(R.string.failed_sign_in), Toast.LENGTH_LONG).show();
                    },
                    error -> {
                        if (error instanceof RetrofitError) {
                            switch (((RetrofitError) error).getResponse().getStatus()) {
                                case 400: // Invalid field or lack of required field.
                                    String json = new String(((TypedByteArray) ((RetrofitError) error).getResponse().getBody()).getBytes());
                                    Gson gson = new Gson();
                                    String errorMessage = "";
                                    if(gson.fromJson(json, SignupError.class).errors.email != null)
                                        errorMessage = getResources().getString(R.string.field_exist_email);
                                    else if(gson.fromJson(json, SignupError.class).errors.nickname != null)
                                        errorMessage = getResources().getString(R.string.field_exist_nickname);
                                    else
                                        errorMessage = getResources().getString(R.string.failed_sign_up);
                                    Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();

                                    if(gson.fromJson(json, SignupError.class).errors.email != null || gson.fromJson(json, SignupError.class).errors.nickname != null) {
                                        mNavigator.navigate(SignUpStep2Fragment.class, true);
                                    }else if(!validateSignUpForm()){
                                    }else{
                                        mNavigator.navigate(SignUpStep1Fragment.class, true);
                                    }
                                    break;
                                case 403: // Failed to SignUp
                                    Toast.makeText(mActivity, getResources().getString(R.string.failed_sign_up), Toast.LENGTH_LONG).show();
                                    break;
                                default:
                                    Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                            }
                        }
                    }
                );
        }
    }

    private boolean validateSignUpForm(){
        String alertMsg;
        int movePosition = -1;
        SignUpForm form = SignUpForm.getInstance();

        if(form.getUniversityId() == null) {
            alertMsg = getResources().getString(R.string.field_invalid_university);
            movePosition = AppConst.Navigator.Auth.SIGNUP_STEP1;
        } else if(form.getEntranceYear() == null) {
            alertMsg = getResources().getString(R.string.field_invalid_entrance_year);
            movePosition = AppConst.Navigator.Auth.SIGNUP_STEP1;
        } else if(form.getValidEmail() == null || !RxValidator.isValidEmail.call(form.getValidEmail())) {
            alertMsg = getResources().getString(R.string.field_invalid_email);
            movePosition = AppConst.Navigator.Auth.SIGNUP_STEP2;
        } else if(form.getValidNickname() == null || !RxValidator.isValidNickname.call(form.getValidNickname())) {
            alertMsg = getResources().getString(R.string.field_invalid_nickname);
            movePosition = AppConst.Navigator.Auth.SIGNUP_STEP2;
        } else if(form.getValidRealname() == null || !RxValidator.isValidRealname.call(form.getValidRealname())) {
            alertMsg = getResources().getString(R.string.field_invalid_realname);
            movePosition = AppConst.Navigator.Auth.SIGNUP_STEP3;
        } else if(form.getValidIsBoy() == null) {
            alertMsg = getResources().getString(R.string.field_invalid_gender);
            movePosition = AppConst.Navigator.Auth.SIGNUP_STEP3;
        } else if(form.getValidPassword() == null) {
            alertMsg = getResources().getString(R.string.field_invalid_password);
        } else {
            return true;
        }

        Toast.makeText(getActivity(), alertMsg, Toast.LENGTH_SHORT).show();
        switch (movePosition) {
            case AppConst.Navigator.Auth.SIGNUP_STEP1 : mNavigator.navigate(SignInFragment.class, false, true); break;
            case AppConst.Navigator.Auth.SIGNUP_STEP2 : mNavigator.navigate(SignInFragment.class, false, true); break;
            case AppConst.Navigator.Auth.SIGNUP_STEP3 : mNavigator.navigate(SignInFragment.class, false, true); break;
        }
        return false;
    }
}
