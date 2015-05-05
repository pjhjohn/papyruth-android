package com.montserrat.parts.auth;

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
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.montserrat.activity.MainActivity;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.controller.AppManager;
import com.montserrat.utils.request.Api;
import com.montserrat.utils.request.ProgressFragment;
import com.montserrat.utils.request.RxVolley;
import com.montserrat.utils.validator.RxValidator;
import com.montserrat.utils.validator.Validator;
import com.montserrat.utils.viewpager.ViewPagerController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Func2;
import rx.subscriptions.CompositeSubscription;

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

    private AutoCompleteTextView vEmail;
    private EditText vPassword;
    private Button btnSignin, btnSignup;
    private CompositeSubscription subscriptions;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth, container, false);

        /* Bind Views */
        this.vEmail = (AutoCompleteTextView) view.findViewById(R.id.auth_email);
        this.vPassword = (EditText) view.findViewById(R.id.auth_password);
        this.vProgress = view.findViewById(R.id.progress_auth);
        this.vContent  = view.findViewById(R.id.content_auth);
        this.btnSignin = (Button) view.findViewById(R.id.btn_sign_in);
        this.btnSignup = (Button) view.findViewById(R.id.btn_sign_up);

        /* Initialize Views */
        this.initEmailAutoComplete();

        /* Bind Listeners to Views */
        this.subscriptions.add(
            Observable
            .combineLatest(
                WidgetObservable.text(vEmail),
                WidgetObservable.text(vPassword),
                (OnTextChangeEvent email, OnTextChangeEvent password) -> {
                    boolean validEmail;
                    if (validEmail = RxValidator.isValidEmail.call(email)) vEmail.setError(null);
                    else vEmail.setError(this.getResources().getString(RxValidator.isEmpty.call(email) ? R.string.field_invalid_required : R.string.field_invalid_email));

                    boolean validPassword;
                    if (validPassword = RxValidator.isValidPassword.call(password)) vPassword.setError(null);
                    else vPassword.setError(this.getResources().getString(RxValidator.isEmpty.call(password) ? R.string.field_invalid_required : R.string.field_invalid_password));

                    return validEmail && validPassword;
                })
            .subscribe(
                valid -> {
                    this.btnSignin.getBackground().setColorFilter(getResources().getColor(valid ? R.color.appDefaultHighlightColor : R.color.appDefaultBackgroundColor), PorterDuff.Mode.MULTIPLY);
                    this.btnSignin.setEnabled(valid);
                }
            )
        );

        subscriptions.add(
            Observable
            .combineLatest(
                ViewObservable.clicks(this.btnSignin).map(unused -> true),
                Observable.create(observer -> this.vPassword.setOnEditorActionListener((TextView v, int action, KeyEvent event) -> {
                    if (action == R.id.signin || action == EditorInfo.IME_NULL || action == EditorInfo.IME_ACTION_DONE) {
                        observer.onNext(true);
                        observer.onCompleted();
                        return true;
                    } return false;
                })),
                (Boolean a, Boolean b) -> a && b
            )
            .flatMap(unused -> {
                this.showProgress(true);
                JSONObject params = new JSONObject();
                try {
                    params.put("email", vEmail.getText().toString())
                        .put("password", vPassword.getText().toString());
                } catch (JSONException ignored) {
                }
                return RxVolley.createObservable(
                    Api.url("users/sign_in"),
                    Request.Method.POST,
                    AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null),
                    params
                );
            })
            .subscribe(response -> {
                this.showProgress(false);
                boolean success = response.optBoolean("success");
                int status = response.optInt("status");
                if (status == 200 && success) {
                    User.getInstance().setData(response.optJSONObject("user"));
                    User.getInstance().setAccessToken(response.optString("access_token", null)); // TODO : Check existance of access-token at setter or
                    AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, response.optString("access_token", null));
                    this.getActivity().startActivity(new Intent(AuthFragment.this.getActivity(), MainActivity.class));
                    this.getActivity().finish();

                } else Toast.makeText(this.getActivity(), this.getResources().getString(R.string.failed_sign_in), Toast.LENGTH_LONG).show();
            })
        );

        subscriptions.add(ViewObservable.clicks(this.btnSignup).subscribe(unused -> this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP1, true)));

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(this.subscriptions != null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
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
                vEmail.setAdapter(adapter);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> cursorLoader) {}
        });
    }
}
