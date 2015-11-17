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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.model.unique.SignUpForm;
import com.papyruth.utils.support.error.ErrorHandler;
import com.papyruth.utils.support.fab.FloatingActionControl;
import com.papyruth.utils.support.picasso.ColorFilterTransformation;
import com.papyruth.utils.support.retrofit.apis.Api;
import com.papyruth.utils.support.rx.RxValidator;
import com.papyruth.utils.view.viewpager.OnPageFocus;
import com.papyruth.utils.view.viewpager.OnPageUnfocus;
import com.papyruth.utils.view.viewpager.ViewPagerController;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep2Fragment extends Fragment implements OnPageFocus, OnPageUnfocus, LoaderManager.LoaderCallbacks<Cursor>{
    private ViewPagerController pagerController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = ((AuthActivity) activity).getViewPagerController();
    }
    @Override
    public void onDetach() {
        super.onDetach();
        this.pagerController = null;
    }

    @InjectView(R.id.email) protected EditText email;
    @InjectView(R.id.nickname) protected EditText nickname;
    @InjectView(R.id.icon_email) protected ImageView iconEmail;
    @InjectView(R.id.icon_nickname) protected ImageView iconNickname;
    private CompositeSubscription subscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step2, container, false);
        ButterKnife.inject(this, view);
        this.subscription = new CompositeSubscription();
        getLoaderManager().initLoader(0, null, this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscription !=null && !this.subscription.isUnsubscribed()) this.subscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        Picasso.with(this.getActivity().getBaseContext()).load(R.drawable.ic_light_email).transform(new ColorFilterTransformation(this.getResources().getColor(R.color.primary_dark_material_dark))).into(this.iconEmail);
        Picasso.with(this.getActivity().getBaseContext()).load(R.drawable.ic_light_person).transform(new ColorFilterTransformation(this.getResources().getColor(R.color.primary_dark_material_dark))).into(this.iconNickname);
        if(this.pagerController.getCurrentPage() == AppConst.ViewPager.Auth.SIGNUP_STEP2){
            ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(this.email, InputMethodManager.SHOW_FORCED);
        }
        this.pagerController.addImeControlFragment(AppConst.ViewPager.Auth.SIGNUP_STEP2);
    }

    private boolean isDuplicateEmail = false;
    private boolean isDuplicateNickname = false;

    private String duplicatedValidator(String email, String nickcname){
        String errorMsg = null;
        this.subscription.add(Api.papyruth()
            .validate((email != null ? "email" : "nickname"), (email != null ? email : nickcname))
            .map(validator -> validator.validation)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                success -> {
                    if(success) {
                        if (email != null) isDuplicateEmail = true;
                        else isDuplicateNickname = true;
                    } else {
                        if (email != null) this.email.setError(getResources().getString(R.string.duplicated_email));
                        else this.nickname.setError(getResources().getString(R.string.duplicated_nickname));
                    }
                    this.showFAC();
                },
                error -> {
                    ErrorHandler.throwError(error, this);
                }
            )
        );
        return errorMsg;
    }

    public void showFAC() {
        String validateNickname = RxValidator.getErrorMessageNickname.call(this.nickname.getText().toString());
        String validateEmail = RxValidator.getErrorMessageEmail.call(this.email.getText().toString());


        boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
        boolean valid = validateEmail == null && validateNickname == null && isDuplicateEmail && isDuplicateNickname;
        if (!visible && valid) {
            FloatingActionControl.getInstance().show(true);
        }else if (visible && !valid) {
            FloatingActionControl.getInstance().hide(true);
        }
    }

    @Override
    public void onPageFocused() {
        FloatingActionControl.getInstance().hide(true);
        ((AuthActivity) getActivity()).setOnShowSoftKeyboard(null);
        ((AuthActivity) getActivity()).setOnHideSoftKeyboard(null);
        InputMethodManager imm = ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        imm.showSoftInput(this.email, InputMethodManager.SHOW_FORCED);

        email.requestFocus();

        if(this.subscription.isUnsubscribed())
            this.subscription = new CompositeSubscription();

        if(SignUpForm.getInstance().getNickname() != null){
            if(emailNotAssigned()) this.email.setText(SignUpForm.getInstance().getEmail());
            this.nickname.setText(SignUpForm.getInstance().getNickname());
            this.duplicatedValidator(email.getText().toString(), null);
            this.duplicatedValidator(null, nickname.getText().toString());
            this.showFAC();
        }else if(this.email.length() > 0 && this.nickname.length() > 0){
            this.duplicatedValidator(email.getText().toString(), null);
            this.duplicatedValidator(null, nickname.getText().toString());
        }


        this.subscription.add(Observable
            .merge(
                WidgetObservable
                .text(this.email)
                .debounce(300, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext(event -> {
                    isDuplicateEmail = false;
                    this.duplicatedValidator(event.text().toString(), null);
                    this.email.setError(RxValidator.getErrorMessageEmail.call(event.text().toString()));
                }),
                WidgetObservable
                .text(this.nickname)
                .debounce(300, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext(event -> {
                    isDuplicateNickname = false;
                    this.duplicatedValidator(null, event.text().toString());
                    this.nickname.setError(RxValidator.getErrorMessageNickname.call(event.text().toString()));
                })
            ).subscribe(event -> {
                this.showFAC();
            }, error->ErrorHandler.throwError(error, this))
        );

        this.subscription.add(
            Observable.mergeDelayError(
                FloatingActionControl.clicks().map(event -> FloatingActionControl.getButton().getVisibility() == View.VISIBLE),
                Observable.create(observer -> this.nickname.setOnEditorActionListener((TextView v, int action, KeyEvent event) -> {
                    observer.onNext(FloatingActionControl.getButton().getVisibility() == View.VISIBLE);
                    return !(FloatingActionControl.getButton().getVisibility() == View.VISIBLE);
                }))
            )
                .filter(use -> use)
                .subscribe(
                    use -> {
                        SignUpForm.getInstance().setEmail(this.email.getText().toString());
                        SignUpForm.getInstance().setNickname(this.nickname.getText().toString());
                        this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP3, true);
                    },
                    error -> ErrorHandler.throwError(error, this))
        );
    }

    @Override
    public void onPageUnfocused() {
        if(this.subscription !=null && !this.subscription.isUnsubscribed()) this.subscription.unsubscribe();
    }

    /* For reading contacts to set initial email of user from device */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arguments) {
        return new CursorLoader(this.getActivity(),
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
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            // Potentially filter on ProfileQuery.IS_PRIMARY
            cursor.moveToNext();
        }
        if(emails.size()>0 && emailNotAssigned()) this.email.setText(emails.get(0));
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
            || this.email.getText() == null
            || this.email.getText().length() <= 0;
    }
}
