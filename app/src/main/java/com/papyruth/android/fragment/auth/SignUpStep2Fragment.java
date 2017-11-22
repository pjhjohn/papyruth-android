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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.model.unique.SignUpForm;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.materialdialog.AlertDialog;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.opensource.rx.RxValidator;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.helper.PermissionHelper;
import com.papyruth.support.utility.navigator.NavigatableLinearLayout;
import com.papyruth.support.utility.navigator.Navigator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep2Fragment extends Fragment {
    private AuthActivity mActivity;
    private Navigator mNavigator;
    private Unbinder mUnbinder;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AuthActivity) activity;
        mNavigator = (Navigator) activity;
    }

    @BindView(R.id.signup_step2_container) protected NavigatableLinearLayout mContainer;
    @BindView(R.id.signup_email_text)    protected EditText mTextEmail;
    @BindView(R.id.signup_nickname_text) protected EditText mTextNickname;
    @BindView(R.id.signup_email_icon)    protected ImageView mIconEmail;
    @BindView(R.id.signup_nickname_icon) protected ImageView mIconNickname;
    private CompositeSubscription mCompositeSubscription;
    private boolean mEmailEdited = false, mNickNameEdited = false;
    private boolean mEmailOfLegacyUser = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step2, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mCompositeSubscription = new CompositeSubscription();
        List<String> emails = getEmails();
        if(emails.size()>0 && emailNotAssigned() && SignUpForm.getInstance().getTempSaveEmail() == null) SignUpForm.getInstance().setTempSaveEmail(emails.get(0));
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        if(mCompositeSubscription ==null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCompositeSubscription.clear();
        mContainer.setOnBackListner(() -> {
            mNavigator.back();
            return true;
        });

        Picasso.with(mActivity).load(R.drawable.ic_email_24dp).transform(new ColorFilterTransformation(mActivity.getResources().getColor(R.color.icon_material))).into(mIconEmail);
        Picasso.with(mActivity).load(R.drawable.ic_nickname_24dp).transform(new ColorFilterTransformation(mActivity.getResources().getColor(R.color.icon_material))).into(mIconNickname);
        mActivity.setCurrentAuthStep(AppConst.Navigator.Auth.SIGNUP_STEP2);

        final View focusedView = mActivity.getWindow().getCurrentFocus();
        Observable.timer(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(
            unused -> ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(focusedView != null ? focusedView : mTextEmail, InputMethodManager.SHOW_FORCED)
        );
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_next).hide(true);
        mCompositeSubscription.add(
            Observable.combineLatest(
                getEmailValidationObservable(mTextEmail).startWith(((String) null)),
                getNicknameValidationObservable(mTextNickname).startWith(((String) null)),
                (String emailError, String nicknameError) -> {
                    if(mEmailEdited)    mTextEmail.setError(emailError);
                    if(mNickNameEdited) mTextNickname.setError(nicknameError);
                    return emailError == null && nicknameError == null && mEmailEdited && mNickNameEdited;
                }
            ).observeOn(AndroidSchedulers.mainThread()).subscribe(valid -> {
                if (valid) mNextButtonEnabled = true;
                boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                if (!visible && valid) FloatingActionControl.getInstance().show(true);
                else if (visible && !valid) FloatingActionControl.getInstance().hide(true);
            }, error -> ErrorHandler.handle(error, this))
        );

        mCompositeSubscription.add(FloatingActionControl.clicks().subscribe(unused -> proceedNextStep()));
        mTextEmail.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_NEXT) {
                mTextNickname.requestFocus();
                return true;
            } return false;
        });
        mTextNickname.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_NEXT) {
                proceedNextStep();
                return true;
            } return false;
        });

        if(mTextEmail.getText().toString().isEmpty()) {
            final String email = SignUpForm.getInstance().getTempSaveEmail();
            if(email != null) mTextEmail.setText(email);
            else mTextEmail.getText().clear();
        } else mTextEmail.setText(mTextEmail.getText());
        mTextEmail.setSelection(mTextEmail.getText().length());

        if(mTextNickname.getText().toString().isEmpty()) {
            final String nickname = SignUpForm.getInstance().getTempSaveNickname();
            if(nickname != null) mTextNickname.setText(nickname);
            else mTextNickname.getText().clear();
        } else mTextNickname.setText(mTextNickname.getText());

        Observable.timer(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(unused -> {
            mTextEmail.requestFocus();
            ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mTextEmail, InputMethodManager.SHOW_FORCED);
        });
    }

    private void proceedNextStep() {
        if(mNextButtonEnabled && mEmailOfLegacyUser) {
            AlertDialog.show(getActivity(), mNavigator, AlertDialog.Type.LEGACY_USER);
        } else if (mNextButtonEnabled) {
            SignUpForm.getInstance().setValidEmail();
            SignUpForm.getInstance().setValidNickname();
            mNavigator.navigate(SignUpStep3Fragment.class, true);
        }
    }

    private boolean mNextButtonEnabled;
    private Observable<String> getEmailValidationObservable(TextView emailTextView) {
        return RxTextView.textChanges(emailTextView)
            .map(charsequence -> {
                mNextButtonEnabled = false;
                return charsequence.toString();
            })
            .flatMap(email -> {
                mEmailEdited = true;
                SignUpForm.getInstance().setTempSaveEmail(email);
                final String errorMessage = RxValidator.getErrorMessageEmail.call(email);
                if (errorMessage == null) return Api.papyruth()
                    .post_users_sign_up_validate("email", email)
                    .map(valid -> {
                        if(valid.validation) {
                            mEmailOfLegacyUser = false;
                            return null;
                        } else switch (valid.status) {
                            case 0:
                                mEmailOfLegacyUser = false;
                                return null;
                            case 1:
                                mEmailOfLegacyUser = false;
                                return getResources().getString(R.string.signup_email_duplication);
                            case 2:
                                mEmailOfLegacyUser = true;
                                return null;
                            default:
                                return getResources().getString(R.string.signup_email_duplication);
                        }
                    });
                else return Observable.just(errorMessage);
            }).observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<String> getNicknameValidationObservable(TextView nicknameTextView) {
        return RxTextView.textChanges(nicknameTextView)
            .map(charsequence -> {
                mNextButtonEnabled = false;
                return charsequence.toString();
            })
            .flatMap(nickname -> {
                mNickNameEdited = true;
                SignUpForm.getInstance().setTempSaveNickname(nickname);
                final String errorMessage = RxValidator.getErrorMessageNickname.call(nickname);
                if (errorMessage == null) return Api.papyruth()
                    .post_users_sign_up_validate("nickname", nickname)
                    .map(validator -> validator)
                    .map(valid -> {
                        if (valid.validation) return null;
                        else switch (valid.status) {
                            case 1 : return getResources().getString(R.string.signup_nickname_duplication);
                            default: return getResources().getString(R.string.signup_nickname_duplication);
                        }
                    });
                else return Observable.just(errorMessage);
            }).observeOn(AndroidSchedulers.mainThread());
    }

    private boolean emailNotAssigned() {
        return SignUpForm.getInstance().getTempSaveEmail() == null
            || SignUpForm.getInstance().getTempSaveEmail().length() <= 0
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
            if(emails.size()>0 && emailNotAssigned() && SignUpForm.getInstance().getTempSaveEmail() == null) SignUpForm.getInstance().setTempSaveEmail(emails.get(0));
        } else PermissionHelper.showRationalDialog(mActivity, PermissionHelper.getRationalMessage(mActivity, PermissionHelper.PERMISSION_GET_ACCOUNTS));
    }
}
