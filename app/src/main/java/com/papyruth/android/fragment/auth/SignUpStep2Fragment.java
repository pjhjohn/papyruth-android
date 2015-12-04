package com.papyruth.android.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import com.papyruth.support.utility.navigator.Navigator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep2Fragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
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
//        getLoaderManager().initLoader(0, null, this);
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
        Picasso.with(mActivity).load(R.drawable.ic_light_email).transform(new ColorFilterTransformation(mActivity.getResources().getColor(R.color.icon_material))).into(mIconEmail);
        Picasso.with(mActivity).load(R.drawable.ic_light_nickname).transform(new ColorFilterTransformation(mActivity.getResources().getColor(R.color.icon_material))).into(mIconNickname);
        final View focusedView = mActivity.getWindow().getCurrentFocus();
        Observable.timer(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(
            unused -> ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(focusedView != null ? focusedView : mTextEmail, InputMethodManager.SHOW_FORCED)
        );
        mActivity.setCurrentSignUpStep(AppConst.Navigator.Auth.SIGNUP_STEP2);

        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_auth_signup2));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_next).hide(true);
        mActivity.setOnShowSoftKeyboard(null);
        mActivity.setOnHideSoftKeyboard(null);
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) mCompositeSubscription = new CompositeSubscription();

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

    /* LoaderManager.LoaderCallbacks<Cursor> : For initial email of user from device */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arguments) {
        return new CursorLoader(
            mActivity,
            // Retrieve data rows for the device user's 'profile' contact.
            Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
            ProfileQuery.PROJECTION,
            // Select only email addresses.
            ContactsContract.Contacts.Data.MIMETYPE + " = ?",
            new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE},
            // Show primary email addresses first. Note that there won't be
            // a primary email address if the user hasn't specified one.
            ContactsContract.Contacts.Data.IS_PRIMARY + " DESC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) emails.add(cursor.getString(ProfileQuery.ADDRESS));
        if(emails.size()>0 && emailNotAssigned() && SignUpForm.getInstance().getEmail() == null) SignUpForm.getInstance().setEmail(emails.get(0));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {}

    private interface ProfileQuery {
        String[] PROJECTION = {
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };
        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    private boolean emailNotAssigned() {
        return SignUpForm.getInstance().getEmail() == null
            || SignUpForm.getInstance().getEmail().length() <= 0
            || mTextEmail.getText() == null
            || mTextEmail.getText().length() <= 0;
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        mActivity.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
