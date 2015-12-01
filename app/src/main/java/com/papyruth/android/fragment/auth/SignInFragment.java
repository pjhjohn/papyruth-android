package com.papyruth.android.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.papyruth;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.opensource.rx.RxValidator;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.viewpager.OnPageFocus;
import com.papyruth.support.utility.viewpager.ViewPagerController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
public class SignInFragment extends Fragment implements OnPageFocus, LoaderManager.LoaderCallbacks<Cursor> {
    private AuthActivity mActivity;
    private ImageView mApplicationLogoHorizontal, mApplicationLogo;
    private ViewPagerController mViewPagerController;
    private Tracker mTracker;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AuthActivity) activity;
        mApplicationLogoHorizontal = (ImageView) mActivity.findViewById(R.id.app_logo_horizontal);
        mApplicationLogo = (ImageView) mActivity.findViewById(R.id.app_logo);
        mViewPagerController = mActivity.getViewPagerController();
        mTracker = ((papyruth) mActivity.getApplication()).getTracker();
    }

    @InjectView (R.id.email)            protected AutoCompleteTextView mTextEmail;
    @InjectView (R.id.password)         protected EditText mTextPassword;
    @InjectView (R.id.progress)         protected View mProgress;
    @InjectView (R.id.sign_in)          protected Button mButtonSignIn;
    @InjectView (R.id.sign_up)          protected Button mButtonSignUp;
    @InjectView (R.id.password_recovery)protected TextView mTextPasswordRecovery;
    private CompositeSubscription mCompositeSubscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signin, container, false);
        ButterKnife.inject(this, view);
        mCompositeSubscriptions = new CompositeSubscription();
        getLoaderManager().initLoader(0, null, this);
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
        if(this.getUserVisibleHint()) onPageFocused();
    }

    @Override
    public void onPageFocused() {
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_auth_signin));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        FloatingActionControl.getInstance().clear();
        mActivity.setOnShowSoftKeyboard(keyboardHeight -> {
            mApplicationLogo.setVisibility(View.GONE);
            mApplicationLogoHorizontal.setVisibility(View.VISIBLE);
        });
        mActivity.setOnHideSoftKeyboard(() -> {
            mApplicationLogo.setVisibility(View.VISIBLE);
            mApplicationLogoHorizontal.setVisibility(View.GONE);
        });
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
                mApplicationLogo.setVisibility(View.VISIBLE);
                mViewPagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP1, true);
            }, error -> ErrorHandler.handle(error, this)
        ));

        this.mCompositeSubscriptions.add(ViewObservable.clicks(this.mTextPasswordRecovery)
            .subscribe(
                event -> new MaterialDialog.Builder(mActivity)
                    .title(R.string.password_recovery_title)
                    .content(R.string.enter_your_email)
                    .input(R.string.hint_email, R.string.empty, (dialog, input) -> {
                    })
                    .positiveText(R.string.submit)
                    .negativeText(R.string.confirm_cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            //TODO : ADD fotgot password api
                        }
                    })
                    .build()
                    .show()
                , error -> ErrorHandler.handle(error, this)
            )
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

    /* LoaderManager.LoaderCallbacks<Cursor> : For email auto-complete suggestion */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
            mActivity,
            Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
            ProfileQuery.PROJECTION,
            ContactsContract.Contacts.Data.MIMETYPE + " = ?",
            new String[] { ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE },
            ContactsContract.Contacts.Data.IS_PRIMARY + " DESC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) emails.add(cursor.getString(ProfileQuery.ADDRESS));
        /* Add to Emails View */
        mTextEmail.setAdapter(new ArrayAdapter<>(mActivity, android.R.layout.simple_dropdown_item_1line, emails));
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
}
