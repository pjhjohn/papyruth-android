package com.papyruth.android.fragment.auth;

import android.app.Activity;
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
import com.google.gson.Gson;
import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.model.error.SignupError;
import com.papyruth.android.model.unique.SignUpForm;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.opensource.rx.RxValidator;
import com.papyruth.support.utility.error.ErrorDefault;
import com.papyruth.support.utility.error.ErrorDefaultRetrofit;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.error.ErrorNetwork;
import com.papyruth.support.utility.fragment.TrackerFragment;
import com.papyruth.support.utility.navigator.NavigatableLinearLayout;
import com.papyruth.support.utility.navigator.Navigator;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
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

public class SignUpStep4Fragment extends TrackerFragment {
    private AuthActivity mActivity;
    private Navigator mNavigator;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AuthActivity) activity;
        mNavigator = (Navigator) activity;
    }

    @Bind(R.id.signup_step4_container)  protected NavigatableLinearLayout mContainer;
    @Bind(R.id.signup_password_text)    protected EditText mTextPassword;
    @Bind(R.id.signup_password_icon)    protected ImageView mIconPassword;
    @Bind(R.id.signup_agreement)        protected TextView mTextAgreement;
    private String mTermsOfUse, mPrivacyPolicy;
    private CompositeSubscription mCompositeSubscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step4, container, false);
        ButterKnife.bind(this, view);
        mCompositeSubscription = new CompositeSubscription();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mContainer.setOnBackListner(() -> {
            mNavigator.back();
            return true;
        });
        Picasso.with(mActivity).load(R.drawable.ic_password_24dp).transform(new ColorFilterTransformation(mActivity.getResources().getColor(R.color.icon_material))).into(mIconPassword);
        InputMethodManager imm = ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE));
        final View focusedView = mActivity.getWindow().getCurrentFocus();
        Observable.timer(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(
            unused -> ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(focusedView != null ? focusedView : mTextPassword, InputMethodManager.SHOW_FORCED)
        );
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_done_green).hide(true);
        FloatingActionControl.getButton().setMax(100);
        FloatingActionControl.getButton().setShowProgressBackground(false);
        mActivity.setCurrentAuthStep(AppConst.Navigator.Auth.SIGNUP_STEP4);
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        Api.papyruth().get_terms().map(terms -> terms.terms).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(
            terms -> {
                if (terms == null || terms.isEmpty()) return;
                mTermsOfUse = terms.get(0).body;
                mPrivacyPolicy = terms.get(1).body;
            }, error -> ErrorHandler.handle(error, this)
        );

        String strTermsOfUse = getString(R.string.signup_terms_of_use);
        String strPrivacyPolicy = getString(R.string.signup_privacy_policy);
        String strAgreement = String.format(getString(R.string.signup_agreement_message), strTermsOfUse, strPrivacyPolicy);
        SpannableString ss = new SpannableString(strAgreement);

        int index;
        index = strAgreement.indexOf(strTermsOfUse);
        ss.setSpan(
            new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    imm.hideSoftInputFromWindow(mTextPassword.getWindowToken(), 0);
                    new MaterialDialog.Builder(mActivity)
                        .title(R.string.signup_terms_of_use)
                        .content(mTermsOfUse)
                        .positiveText(R.string.dialog_positive_ok)
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
                        .title(R.string.signup_privacy_policy)
                        .content(mPrivacyPolicy)
                        .positiveText(R.string.dialog_positive_ok)
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

        mCompositeSubscription.add(FloatingActionControl.clicks().subscribe(unused -> submitSignUpForm()));
        mCompositeSubscription.add(getPasswordValidationObservable(mTextPassword)
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

        mTextPassword.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_NEXT) {
                submitSignUpForm();
                return true;
            } return false;
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

    private boolean mSubmitButtonEnabled;
    private Observable<String> getPasswordValidationObservable(TextView passwordTextView) {
        return WidgetObservable.text(passwordTextView)
            .map(event -> {
                mSubmitButtonEnabled = false;
                SignUpForm.getInstance().setTempSavePassword(event.text().toString());
                return event.text().toString();
            })
            .map(RxValidator.getErrorMessagePassword)
            .observeOn(AndroidSchedulers.mainThread());
    }

    private void submitSignUpForm() {
        if(mSubmitButtonEnabled) SignUpForm.getInstance().setValidPassword();
        if(!validateSignUpForm()) return;
        FloatingActionControl.getButton().setIndeterminate(true);
        Api.papyruth().post_users_sign_up(
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
                    FloatingActionControl.getButton().setIndeterminate(false);
                    FloatingActionControl.getButton().setProgress(0, true);
                    if (response.success) {
                        User.getInstance().update(response.user, response.access_token);
                        AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, response.access_token);
                        SignUpForm.getInstance().clear();
                        mActivity.startMainActivity();
                    } else Toast.makeText(mActivity, getResources().getString(R.string.toast_signin_failed), Toast.LENGTH_SHORT).show();
                },
                error -> {
                    FloatingActionControl.getButton().setIndeterminate(false);
                    FloatingActionControl.getButton().setProgress(0, true);
                    if (error instanceof RetrofitError) {
                        switch (((RetrofitError) error).getKind()) {
                            case HTTP:
                                switch (((RetrofitError) error).getResponse().getStatus()) {
                                    case 400: // Invalid field or lack of required field.
                                        SignupError signupError = new Gson().fromJson(
                                                new String(((TypedByteArray) ((RetrofitError) error).getResponse().getBody()).getBytes()),
                                                SignupError.class
                                        );
                                        Toast.makeText(mActivity, signupError.errors.email != null ? R.string.signup_email_duplication : (signupError.errors.nickname != null ? R.string.signup_nickname_duplication : R.string.toast_signup_failed), Toast.LENGTH_SHORT).show();
                                        if (signupError.errors.email != null || signupError.errors.nickname != null) {
                                            mNavigator.navigate(SignUpStep2Fragment.class, true);
                                        } else if (!validateSignUpForm()) {
                                        } else {
                                            mNavigator.navigate(SignUpStep1Fragment.class, true);
                                        }
                                        break;
                                    case 403: // Failed to SignUp
                                        Toast.makeText(mActivity, getResources().getString(R.string.toast_signup_failed), Toast.LENGTH_SHORT).show();
                                        break;
                                    default:
                                        Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                                }
                                break;
                            case NETWORK:
                                if( ErrorNetwork.handle(((RetrofitError) error.getCause()), this).handled ) Toast.makeText(mActivity, R.string.toast_error_retrofit_unstable_network, Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                ErrorDefaultRetrofit.handle(((RetrofitError) error.getCause()), this);
                                break;
                        }
                    }else{
                        ErrorDefault.handle(error.getCause(), this);
                    }
                }
            );
    }

    private boolean validateSignUpForm() {
        String alertMsg;
        int target = -1;
        SignUpForm signupForm = SignUpForm.getInstance();

        if(signupForm.getUniversityId() == null) {
            alertMsg = getResources().getString(R.string.signup_invalid_university);
            target = AppConst.Navigator.Auth.SIGNUP_STEP1;
        } else if(signupForm.getEntranceYear() == null) {
            alertMsg = getResources().getString(R.string.signup_invalid_entrance_year);
            target = AppConst.Navigator.Auth.SIGNUP_STEP1;
        } else if(signupForm.getValidEmail() == null || !RxValidator.isValidEmail.call(signupForm.getValidEmail())) {
            alertMsg = getResources().getString(R.string.signup_invalid_email);
            target = AppConst.Navigator.Auth.SIGNUP_STEP2;
        } else if(signupForm.getValidNickname() == null || !RxValidator.isValidNickname.call(signupForm.getValidNickname())) {
            alertMsg = getResources().getString(R.string.signup_invalid_nickname);
            target = AppConst.Navigator.Auth.SIGNUP_STEP2;
        } else if(signupForm.getValidRealname() == null || !RxValidator.isValidRealname.call(signupForm.getValidRealname())) {
            alertMsg = getResources().getString(R.string.signup_invalid_realname);
            target = AppConst.Navigator.Auth.SIGNUP_STEP3;
        } else if(signupForm.getValidIsBoy() == null) {
            alertMsg = getResources().getString(R.string.signup_invalid_gender);
            target = AppConst.Navigator.Auth.SIGNUP_STEP3;
        } else if(signupForm.getValidPassword() == null) {
            alertMsg = getResources().getString(R.string.signup_invalid_password);
        } else return true;

        Toast.makeText(mActivity, alertMsg, Toast.LENGTH_SHORT).show();
        switch (target) {
            case AppConst.Navigator.Auth.SIGNUP_STEP1 : mNavigator.navigate(SignInFragment.class, true, true); break;
            case AppConst.Navigator.Auth.SIGNUP_STEP2 : mNavigator.navigate(SignInFragment.class, true, true); break;
            case AppConst.Navigator.Auth.SIGNUP_STEP3 : mNavigator.navigate(SignInFragment.class, true, true); break;
        }
        return false;
    }
}
