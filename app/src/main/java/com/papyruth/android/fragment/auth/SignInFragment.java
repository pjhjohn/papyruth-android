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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.PapyruthApplication;
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.materialdialog.InputDialog;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.opensource.rx.RxValidator;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.helper.PermissionHelper;
import com.papyruth.support.utility.navigator.Navigator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.papyruth.support.opensource.rx.RxValidator.toString;

/**
 * Created by mrl on 2015-04-07.
 */
public class SignInFragment extends Fragment {
    private AuthActivity mActivity;
    private Navigator mNavigator;
    private Tracker mTracker;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AuthActivity) activity;
        mNavigator = (Navigator) activity;
        mTracker = ((PapyruthApplication) mActivity.getApplication()).getTracker();
    }

    @InjectView (R.id.signin_email_text)        protected AutoCompleteTextView mTextEmail;
    @InjectView (R.id.signin_password_text)     protected EditText mTextPassword;
    @InjectView (R.id.signin_button)            protected Button mButtonSignIn;
    @InjectView (R.id.signin_signup_button)     protected Button mButtonSignUp;
    @InjectView (R.id.signin_password_recovery) protected TextView mTextPasswordRecovery;
    @InjectView (R.id.material_progress_large)  protected View mProgress;
    private CompositeSubscription mCompositeSubscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signin, container, false);
        ButterKnife.inject(this, view);
        mCompositeSubscriptions = new CompositeSubscription();
        mTextEmail.setAdapter(new ArrayAdapter<>(mActivity, android.R.layout.simple_dropdown_item_1line, getEmails()));
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(mCompositeSubscriptions ==null || mCompositeSubscriptions.isUnsubscribed()) return;
        mCompositeSubscriptions.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_auth_signin));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mActivity.setCurrentAuthStep(AppConst.Navigator.Auth.SIGNIN);
        FloatingActionControl.getInstance().clear();
        mCompositeSubscriptions.add(Observable.combineLatest(
            WidgetObservable.text(mTextEmail).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessageEmail),
            WidgetObservable.text(mTextPassword).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessagePassword),
            (String emailError, String passwordError) -> {
                mTextEmail.setError(emailError);
                mTextPassword.setError(passwordError);
                return emailError == null && passwordError == null;
            })
            .startWith(false)
            .subscribe(mButtonSignIn::setEnabled, error -> ErrorHandler.handle(error, this))
        );

        mCompositeSubscriptions.add(
            Observable.mergeDelayError(
                ViewObservable.clicks(mButtonSignIn).map(unused -> mButtonSignIn.isEnabled()),
                Observable.create(observer -> mTextPassword.setOnEditorActionListener((TextView v, int action, KeyEvent event) -> {
                    observer.onNext(mButtonSignIn.isEnabled());
                    observer.onCompleted();
                    return !mButtonSignIn.isEnabled();
                }))
            )
            .filter(trigger -> trigger)
            .subscribe(unused -> requestSignIn(), error -> ErrorHandler.handle(error, this))
        );

        mCompositeSubscriptions.add(ViewObservable.clicks(mButtonSignUp).subscribe(
            unused -> {
                mNavigator.navigate(SignUpStep1Fragment.class, true);
            }, error -> ErrorHandler.handle(error, this)
        ));

        mCompositeSubscriptions.add(ViewObservable.clicks(this.mTextPasswordRecovery)
            .subscribe(event -> InputDialog.show(mActivity), error -> ErrorHandler.handle(error, this))
        );
    }

    private void requestSignIn() {
        AnimatorHelper.FADE_IN(mProgress).start();
        mCompositeSubscriptions.add(Api.papyruth()
            .users_sign_in(mTextEmail.getText().toString(), mTextPassword.getText().toString())
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
                        .users_refresh_token(User.getInstance().getAccessToken())
                        .map(user -> user.access_token)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            token -> {
                                User.getInstance().setAccessToken(token);
                                AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, token);
                                mActivity.startMainActivity();
                            }, error -> {
                                Timber.d("refresh error : %s", error);
                                error.printStackTrace();
                            }
                        );
                    else Toast.makeText(mActivity, this.getResources().getString(R.string.failed_sign_in), Toast.LENGTH_LONG).show();
                },
                error -> {
                    AnimatorHelper.FADE_OUT(mProgress).start();
                    if (error instanceof RetrofitError) {
                        switch (((RetrofitError) error).getResponse().getStatus()) {
                            case 403:
                                Toast.makeText(mActivity, this.getResources().getString(R.string.failed_sign_in), Toast.LENGTH_LONG).show();
                                break;
                            default:
                                Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                        }
                    }
                }
            )
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
