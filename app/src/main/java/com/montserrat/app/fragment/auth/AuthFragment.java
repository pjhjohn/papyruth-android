package com.montserrat.app.fragment.auth;

import android.app.Activity;
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
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.montserrat.app.activity.MainActivity;
import com.montserrat.app.R;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.model.User;
import com.montserrat.utils.request.Api;
import com.montserrat.utils.request.ProgressFragment;
import com.montserrat.utils.request.RxVolley;
import com.montserrat.utils.validator.RxValidator;
import com.montserrat.utils.viewpager.ViewPagerController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.android.widget.WidgetObservable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.montserrat.utils.validator.RxValidator.toString;

/**
 * Created by mrl on 2015-04-07.
 */
public class AuthFragment extends ProgressFragment {
    /* Set PageController */
    private ViewPagerController pagerController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
        this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @InjectView (R.id.email) protected AutoCompleteTextView emailField;
    @InjectView (R.id.password) protected EditText passwordField;
    @InjectView (R.id.progress) protected View progress;
    @InjectView (R.id.submit) protected Button submit;
    @InjectView (R.id.signup) protected Button signup;
    private CompositeSubscription subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.initEmailAutoComplete();
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.subscriptions.add( Observable.combineLatest(
            WidgetObservable.text(emailField).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessageEmail),
            WidgetObservable.text(passwordField).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessagePassword),
            (String emailError, String passwordError) -> {
                emailField.setError(emailError);
                passwordField.setError(passwordError);
                return emailError==null && passwordError==null;
            })
            .startWith( false )
            .subscribe( valid -> {
                this.submit.getBackground().setColorFilter(getResources().getColor(
                    valid
                    ? R.color.fg_accent
                    : R.color.transparent
                ), PorterDuff.Mode.MULTIPLY);
                this.submit.setEnabled(valid);
            })
        );

        subscriptions.add( Observable
            .merge(
                ViewObservable.clicks(this.submit).map(unused -> this.submit.isEnabled()),
                Observable.create(observer -> this.passwordField.setOnEditorActionListener((TextView v, int action, KeyEvent event) -> {
                    observer.onNext(this.submit.isEnabled());
                    observer.onCompleted();
                    return !this.submit.isEnabled();
                }))
            )
            .filter( trigger -> trigger )
            .flatMap( unused -> {
                this.showProgress(this.progress, true);
                JSONObject params = new JSONObject();
                try { params.put("email", emailField.getText().toString()).put("password", passwordField.getText().toString()); }
                catch (JSONException ignored) {}
                return RxVolley.createObservable(
                    Api.url("users/sign_in"),
                    Request.Method.POST,
                    AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null),
                    params
                );
            })
            .subscribe(response -> {
                this.showProgress(this.progress, false);
                switch (response.optInt("status")) {
                    case 200:
                        if (response.optBoolean("success")) {
                            User.getInstance().setData(response.optJSONObject("user"));
                            User.getInstance().setAccessToken(response.optString("access_token", null)); // TODO : Check existance of access-token at setter or
                            AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, response.optString("access_token", null));
                            this.getActivity().startActivity(new Intent(AuthFragment.this.getActivity(), MainActivity.class));
                            this.getActivity().finish();
                        } else {
                            Toast.makeText(this.getActivity(), this.getResources().getString(R.string.failed_sign_in), Toast.LENGTH_LONG).show();
                            // TODO : Implement action for signin failure
                        }
                        break;
                    case 403 :
                        Toast.makeText(this.getActivity(), this.getResources().getString(R.string.failed_sign_in), Toast.LENGTH_LONG).show();
                        // TODO : Implement action for signin failure
                        break;
                    default :
                        Timber.e("Unexpected Status code : %d - Needs to be implemented", response.optInt("status"));
                }
            })
        );

        subscriptions.add(ViewObservable.clicks(this.signup).subscribe(unused -> this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP1, true)));
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
                    AuthFragment.this.getActivity(),
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
                    AuthFragment.this.getActivity(),
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
