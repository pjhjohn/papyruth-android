package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.activity.AuthActivity;
import com.montserrat.app.activity.MainActivity;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.support.rx.RxValidator;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.rengwuxian.materialedittext.MaterialEditText;

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

import static com.montserrat.utils.support.rx.RxValidator.toString;

/**
 * Created by mrl on 2015-04-07.
 */
public class SignInFragment extends Fragment implements OnPageFocus {
    /* Set PageController */
    private ViewPagerController pagerController;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    @InjectView (R.id.email) protected MaterialAutoCompleteTextView emailField;
    @InjectView (R.id.password) protected MaterialEditText passwordField;
    @InjectView (R.id.progress) protected View progress;
    @InjectView (R.id.sign_in) protected ButtonFlat signin;
    @InjectView (R.id.sign_up) protected ButtonFlat signup;
    private CompositeSubscription subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.initEmailAutoComplete();
        this.signin.setRippleSpeed(50.0f);
        this.signup.setRippleSpeed(50.0f);
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    private void doRequest() {
        this.progress.setVisibility(View.VISIBLE);
        this.subscriptions.add(
            RetrofitApi.getInstance().users_sign_in(emailField.getText().toString(), passwordField.getText().toString())
            .map(response -> {
                User.getInstance().update(response.user, response.access_token);
                AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, response.access_token);
                return response.success;
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                success -> {
                    this.progress.setVisibility(View.GONE);
                    if (success) {
                        this.subscriptions.add(
                            RetrofitApi.getInstance().refresh_token(User.getInstance().getAccessToken())
                                .map(user -> user.access_token)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(token -> {
                                        User.getInstance().setAccessToken(token);
                                        AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, token);
                                        this.getActivity().startActivity(new Intent(this.getActivity(), MainActivity.class));
                                        this.getActivity().finish();
                                    },error -> {
                                        Timber.d("refresh error : %s", error);
                                        error.printStackTrace();
                                    }
                                )
                        );
                    } else {
                        Toast.makeText(this.getActivity(), this.getResources().getString(R.string.failed_sign_in), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    this.progress.setVisibility(View.GONE);
                    if (error instanceof RetrofitError) {
                        switch (((RetrofitError) error).getResponse().getStatus()) {
                            case 403:
                                Toast.makeText(this.getActivity(), this.getResources().getString(R.string.failed_sign_in), Toast.LENGTH_LONG).show();
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
    public void onPageFocused() {
        ((AuthActivity)this.getActivity()).signUpStep(-1);
        FloatingActionControl.getInstance().clear();
        this.subscriptions.add(Observable.combineLatest(
            WidgetObservable.text(emailField).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessageEmail),
            WidgetObservable.text(passwordField).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessagePassword),
            (String emailError, String passwordError) -> {
                emailField.setError(emailError);
                passwordField.setError(passwordError);
                return emailError == null && passwordError == null;
            })
            .startWith(false)
            .subscribe(valid -> {
                this.signin.getBackground().setColorFilter(getResources().getColor(valid? R.color.fg_accent : R.color.white), PorterDuff.Mode.MULTIPLY);
                this.signin.setEnabled(valid);
            })
        );

        subscriptions.add(
            Observable.mergeDelayError(
                ViewObservable.clicks(this.signin).map(unused -> this.signin.isEnabled()),
                Observable.create(observer -> this.passwordField.setOnEditorActionListener((TextView v, int action, KeyEvent event) -> {
                    observer.onNext(this.signin.isEnabled());
                    observer.onCompleted();
                    return !this.signin.isEnabled();
                }))
            )
            .filter(trigger -> trigger)
            .subscribe(unused -> doRequest()));

        subscriptions.add(ViewObservable.clicks(this.signup).subscribe(
            unused ->
//                this.navigator.navigate(SignUpStepUnivFragment.class, true)
                this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_UNIV, true)
        ));
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };
        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    private void initEmailAutoComplete() {
        this.getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(
                    SignInFragment.this.getActivity(),
                    Uri.withAppendedPath(
                        ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY
                    ),
                    ProfileQuery.PROJECTION,
                    ContactsContract.Contacts.Data.MIMETYPE + " = ?",
                    new String[] { ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE },
                    ContactsContract.Contacts.Data.IS_PRIMARY + " DESC"
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
                List<String> emails = new ArrayList<>();
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    emails.add(cursor.getString(ProfileQuery.ADDRESS));
                    cursor.moveToNext();
                }
                /* Add to Emails View */
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    SignInFragment.this.getActivity(),
                    android.R.layout.simple_dropdown_item_1line,
                    emails
                );
                emailField.setAdapter(adapter);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> cursorLoader) {}
        });
    }
}
