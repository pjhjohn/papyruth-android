package com.papyruth.android.fragment.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.model.unique.SignUpForm;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.materialdialog.PasswordRecoveryDialog;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.opensource.rx.RxValidator;
import com.papyruth.support.utility.error.Error403;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.error.ErrorNetwork;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.helper.PermissionHelper;
import com.papyruth.support.utility.navigator.Navigator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by mrl on 2015-04-07.
 */
public class SignInFragment extends Fragment {
    private AuthActivity mActivity;
    private Navigator mNavigator;
    private Unbinder mUnbinder;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AuthActivity) activity;
        mNavigator = (Navigator) activity;
    }

    @BindView(R.id.signin_email_text)        protected AutoCompleteTextView mTextEmail;
    @BindView(R.id.signin_password_text)     protected EditText mTextPassword;
    @BindView(R.id.signin_button)            protected Button mButtonSignIn;
    @BindView(R.id.signin_signup_button)     protected Button mButtonSignUp;
    @BindView(R.id.signin_password_recovery) protected TextView mTextPasswordRecovery;
    @BindView(R.id.material_progress_large)  protected View mProgress;
    private CompositeSubscription mCompositeSubscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signin, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mCompositeSubscriptions = new CompositeSubscription();
        mTextEmail.setAdapter(new ArrayAdapter<>(mActivity, android.R.layout.simple_dropdown_item_1line, getEmails()));
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        if(mCompositeSubscriptions ==null || mCompositeSubscriptions.isUnsubscribed()) return;
        mCompositeSubscriptions.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.setCurrentAuthStep(AppConst.Navigator.Auth.SIGNIN);
        SignUpForm.getInstance().clear();
        FloatingActionControl.getInstance().clear();
        mCompositeSubscriptions.clear();
        mCompositeSubscriptions.add(Observable.combineLatest(
            RxTextView.textChanges(mTextEmail).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(CharSequence::toString).map(RxValidator.getErrorMessageEmail),
            RxTextView.textChanges(mTextPassword).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(CharSequence::toString).map(RxValidator.getErrorMessagePassword),
            (String emailError, String passwordError) -> {
                mTextEmail.setError(emailError);
                mTextPassword.setError(passwordError);
                return emailError == null && passwordError == null;
            })
            .startWith(false)
            .subscribe(mButtonSignIn::setEnabled, error -> ErrorHandler.handle(error, this))
        );

        mCompositeSubscriptions.add(Observable.mergeDelayError(
            RxView.clicks(mButtonSignIn).map(unused -> mButtonSignIn.isEnabled()),
            Observable.create(observer -> mTextPassword.setOnEditorActionListener((TextView v, int action, KeyEvent event) -> {
                observer.onNext(mButtonSignIn.isEnabled());
                observer.onCompleted();
                return !mButtonSignIn.isEnabled();
            })))
            .filter(trigger -> trigger)
            .subscribe(unused -> requestSignIn(), error -> ErrorHandler.handle(error, this))
        );

        mCompositeSubscriptions.add(RxView.clicks(mButtonSignUp).subscribe(
            unused -> mNavigator.navigate(SignUpStep1Fragment.class, true), error -> ErrorHandler.handle(error, this)
        ));

        mCompositeSubscriptions.add(RxView.clicks(this.mTextPasswordRecovery)
            .subscribe(event -> PasswordRecoveryDialog.show(mActivity), error -> ErrorHandler.handle(error, this))
        );
    }

    private void requestSignIn() {
        AnimatorHelper.FADE_IN(mProgress).start();
        Api.papyruth()
            .post_users_sign_in(
                mTextEmail.getText().toString(),
                mTextPassword.getText().toString(),
                AppConst.DEVICE_TYPE,
                AppManager.getInstance().getAppVersion(getActivity()),
                Build.VERSION.RELEASE,
                Build.MODEL
            )
            .map(response -> {
                User.getInstance().update(response.user, response.access_token);
                AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, response.access_token);
                return response.success;
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                success -> {
                    AnimatorHelper.FADE_OUT(mProgress).start();
                    if (success) Api.papyruth()
                        .post_users_refresh_token(
                            User.getInstance().getAccessToken(),
                            AppConst.DEVICE_TYPE,
                            AppManager.getInstance().getAppVersion(getActivity()),
                            Build.VERSION.RELEASE,
                            Build.MODEL
                        )
                        .map(user -> user.access_token)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            token -> {
                                User.getInstance().setAccessToken(token);
                                AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, token);
                                mActivity.startMainActivity();
                            }, error -> ErrorHandler.handle(error, this, true)
                        );
                    else Toast.makeText(mActivity, this.getResources().getString(R.string.toast_signin_failed), Toast.LENGTH_SHORT).show();
                },
                error -> {
                    AnimatorHelper.FADE_OUT(mProgress).start();
                    if (error instanceof RetrofitError) {
                        final RetrofitError throwable = (RetrofitError) error;
                        if(ErrorNetwork.handle(throwable, this, true).handled) return;
                        if(Error403.handle(throwable, this).handled) Toast.makeText(mActivity, this.getResources().getString(R.string.toast_signin_failed), Toast.LENGTH_SHORT).show();
                        else Toast.makeText(mActivity, this.getResources().getString(R.string.toast_error_default), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private List<String> getEmails() {
        List<String> emails = new ArrayList<>();
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        for (Account account : getAccounts()) {
            if (emailPattern.matcher(account.name).matches()) {
                if(emails.contains(account.name)) continue;
                emails.add(account.name);
            }
        } return emails;
    }

    private Account[] getAccounts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return AccountManager.get(mActivity).getAccounts();
        else if (PermissionHelper.checkAndRequestPermission(this, PermissionHelper.PERMISSION_GET_ACCOUNTS, android.Manifest.permission.GET_ACCOUNTS)) {
            return AccountManager.get(mActivity).getAccounts();
        } else return new Account[0];
    }

    /* API23+ Runtime Permission */
    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != PermissionHelper.PERMISSION_GET_ACCOUNTS) return;
        if (PermissionHelper.verifyPermissions(grantResults)) {
            mTextEmail.setAdapter(new ArrayAdapter<>(mActivity, android.R.layout.simple_dropdown_item_1line, getEmails()));
        } else PermissionHelper.showRationalDialog(mActivity, PermissionHelper.getRationalMessage(mActivity, PermissionHelper.PERMISSION_GET_ACCOUNTS));
    }
}
