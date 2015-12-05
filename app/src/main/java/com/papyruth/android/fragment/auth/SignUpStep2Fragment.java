package com.papyruth.android.fragment.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.PapyruthApplication;
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.model.unique.SignUpForm;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.opensource.rx.RxValidator;
import com.papyruth.support.utility.helper.PermissionHelper;
import com.papyruth.support.utility.navigator.NavigatableLinearLayout;
import com.papyruth.support.utility.navigator.Navigator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep2Fragment extends Fragment {
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

    @InjectView(R.id.signup_step2_container) protected NavigatableLinearLayout mContainer;
    @InjectView(R.id.signup_email_text)    protected EditText mTextEmail;
    @InjectView(R.id.signup_nickname_text) protected EditText mTextNickname;
    @InjectView(R.id.signup_email_icon)    protected ImageView mIconEmail;
    @InjectView(R.id.signup_nickname_icon) protected ImageView mIconNickname;
    private CompositeSubscription mCompositeSubscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step2, container, false);
        ButterKnife.inject(this, view);
        mCompositeSubscription = new CompositeSubscription();
        List<String> emails = getEmails();
        if(emails.size()>0 && emailNotAssigned() && SignUpForm.getInstance().getEmail() == null) SignUpForm.getInstance().setEmail(emails.get(0));
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(mCompositeSubscription ==null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mContainer.setOnBackListner(() -> {
            this.mNavigator.navigate(SignUpStep1Fragment.class, false, Navigator.AnimatorType.SLIDE_TO_LEFT);
            return true;
        });

        Picasso.with(mActivity).load(R.drawable.ic_light_email).transform(new ColorFilterTransformation(mActivity.getResources().getColor(R.color.icon_material))).into(mIconEmail);
        Picasso.with(mActivity).load(R.drawable.ic_light_nickname).transform(new ColorFilterTransformation(mActivity.getResources().getColor(R.color.icon_material))).into(mIconNickname);
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_auth_signup2));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mActivity.setCurrentAuthStep(AppConst.Navigator.Auth.SIGNUP_STEP2);
        final View focusedView = mActivity.getWindow().getCurrentFocus();
        Observable.timer(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(
            unused -> ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(focusedView != null ? focusedView : mTextEmail, InputMethodManager.SHOW_FORCED)
        );
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_next).hide(true);

        mCompositeSubscription.add(
            Observable.combineLatest(
                getEmailValidationObservable(mTextEmail),
                getNicknameValidationObservable(mTextNickname),
                (String emailError, String nicknameError) -> {
                    mTextEmail.setError(emailError);
                    mTextNickname.setError(nicknameError);
                    return emailError == null && nicknameError == null;
                }
            ).observeOn(AndroidSchedulers.mainThread()).subscribe(valid -> {
                if (valid) mNextButtonEnabled = true;
                boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                if (!visible && valid) FloatingActionControl.getInstance().show(true);
                else if (visible && !valid) FloatingActionControl.getInstance().hide(true);
            }, Throwable::printStackTrace)
        );

        mCompositeSubscription.add(FloatingActionControl.clicks().subscribe(
            unused -> {
                if (mNextButtonEnabled) {
                    SignUpForm.getInstance().setEmail(mTextEmail.getText().toString());
                    SignUpForm.getInstance().setNickname(mTextNickname.getText().toString());
                    mNavigator.navigate(SignUpStep3Fragment.class, true);
                }
            }
        ));

        if(mTextEmail.getText().toString().isEmpty()) {
            final String email = SignUpForm.getInstance().getEmail();
            if(email != null) mTextEmail.setText(email);
            else mTextEmail.getText().clear();
        } else mTextEmail.setText(mTextEmail.getText());
        if(mTextNickname.getText().toString().isEmpty()) {
            final String nickname = SignUpForm.getInstance().getNickname();
            if(nickname != null) mTextNickname.setText(nickname);
            else mTextNickname.getText().clear();
        } else mTextNickname.setText(mTextNickname.getText());

        Observable.timer(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(unused -> {
            mTextEmail.requestFocus();
            ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mTextEmail, InputMethodManager.SHOW_FORCED);
        });
    }

    private boolean mNextButtonEnabled;
    private Observable<String> getEmailValidationObservable(TextView emailTextView) {
        return WidgetObservable.text(emailTextView)
            .map(event -> {
                mNextButtonEnabled = false;
                return event;
            })
            .map(event -> event.text().toString())
            .flatMap(email -> {
                final String errorMessage = RxValidator.getErrorMessageEmail.call(email);
                if (errorMessage == null) return Api.papyruth()
                    .users_sign_up_validate("email", email)
                    .map(validator -> validator.validation)
                    .map(valid -> valid ? null : getResources().getString(R.string.duplicated_email));
                else return Observable.just(errorMessage);
            }).observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<String> getNicknameValidationObservable(TextView nicknameTextView) {
        return WidgetObservable.text(nicknameTextView)
            .map(event -> {
                mNextButtonEnabled = false;
                return event;
            })
            .map(event -> event.text().toString())
            .flatMap(nickname -> {
                final String errorMessage = RxValidator.getErrorMessageNickname.call(nickname);
                if (errorMessage == null) return Api.papyruth()
                    .users_sign_up_validate("nickname", nickname)
                    .map(validator -> validator.validation)
                    .map(valid -> valid ? null : getResources().getString(R.string.duplicated_nickname));
                else return Observable.just(errorMessage);
            }).observeOn(AndroidSchedulers.mainThread());
    }

    private boolean emailNotAssigned() {
        return SignUpForm.getInstance().getEmail() == null
            || SignUpForm.getInstance().getEmail().length() <= 0
            || mTextEmail.getText() == null
            || mTextEmail.getText().length() <= 0;
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
            List<String> emails = getEmails();
            if(emails.size()>0 && emailNotAssigned() && SignUpForm.getInstance().getEmail() == null) SignUpForm.getInstance().setEmail(emails.get(0));
        } else PermissionHelper.showRationalDialog(mActivity, PermissionHelper.getRationalMessage(mActivity, PermissionHelper.PERMISSION_GET_ACCOUNTS));
    }
}
