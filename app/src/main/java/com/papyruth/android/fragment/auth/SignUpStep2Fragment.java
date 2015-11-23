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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.model.unique.SignUpForm;
import com.papyruth.android.papyruth;
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

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep2Fragment extends Fragment implements OnPageFocus, OnPageUnfocus, LoaderManager.LoaderCallbacks<Cursor>{

    private ViewPagerController mViewPagerController;
    private Context mContext;
    private Tracker mTracker;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = ((papyruth) getActivity().getApplication()).getTracker();
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mViewPagerController = ((AuthActivity) activity).getViewPagerController();
        mContext = activity;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mViewPagerController = null;
        mContext = null;
    }

    @InjectView(R.id.email)         protected EditText mTextEmail;
    @InjectView(R.id.nickname)      protected EditText mTextNickname;
    @InjectView(R.id.icon_email)    protected ImageView mIconEmail;
    @InjectView(R.id.icon_nickname) protected ImageView mIconNickname;
    private CompositeSubscription mCompositeSubscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step2, container, false);
        ButterKnife.inject(this, view);
        mCompositeSubscription = new CompositeSubscription();
        getLoaderManager().initLoader(0, null, this);
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
        Picasso.with(mContext).load(R.drawable.ic_light_email).transform(new ColorFilterTransformation(mContext.getResources().getColor(R.color.icon_material))).into(mIconEmail);
        Picasso.with(mContext).load(R.drawable.ic_light_nickname).transform(new ColorFilterTransformation(mContext.getResources().getColor(R.color.icon_material))).into(mIconNickname);
        if(mViewPagerController.getCurrentPage() == AppConst.ViewPager.Auth.SIGNUP_STEP2) ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mTextEmail, InputMethodManager.SHOW_FORCED);
        mViewPagerController.addImeControlFragment(AppConst.ViewPager.Auth.SIGNUP_STEP2);
    }

    private boolean mDuplicatedEmail = false;
    private boolean mDuplicatedNickname = false;

    /* TODO : Validate Separately. email != null is not a good way to check type */
    private String validateDuplication(String email, String nickcname){
        String errorMsg = null;
        mCompositeSubscription.add(Api.papyruth()
            .validate((email != null ? "email" : "nickname"), (email != null ? email : nickcname))
            .map(validator -> validator.validation)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                success -> {
                    if (success) {
                        if (email != null) mDuplicatedEmail = true;
                        else mDuplicatedNickname = true;
                    } else {
                        if (email != null) mTextEmail.setError(getResources().getString(R.string.duplicated_email));
                        else mTextNickname.setError(getResources().getString(R.string.duplicated_nickname));
                    }
                    showFAC();
                }, error -> ErrorHandler.throwError(error, this)
            )
        );
        return errorMsg;
    }

    public void showFAC() {
        String validateNickname = RxValidator.getErrorMessageNickname.call(mTextNickname.getText().toString());
        String validateEmail = RxValidator.getErrorMessageEmail.call(mTextEmail.getText().toString());

        boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
        boolean valid = validateEmail == null && validateNickname == null && mDuplicatedEmail && mDuplicatedNickname;
        if (!visible && valid) FloatingActionControl.getInstance().show(true);
        else if (visible && !valid) FloatingActionControl.getInstance().hide(true);
    }

    @Override
    public void onPageFocused() {
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_auth_signup2));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        FloatingActionControl.getInstance().hide(true);
        ((AuthActivity) getActivity()).setOnShowSoftKeyboard(null);
        ((AuthActivity) getActivity()).setOnHideSoftKeyboard(null);
        InputMethodManager imm = ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        imm.showSoftInput(mTextEmail, InputMethodManager.SHOW_FORCED);

        mTextEmail.requestFocus();

        if(mCompositeSubscription.isUnsubscribed())
            mCompositeSubscription = new CompositeSubscription();

        if(SignUpForm.getInstance().getNickname() != null){
            if(emailNotAssigned()) mTextEmail.setText(SignUpForm.getInstance().getEmail());
            mTextNickname.setText(SignUpForm.getInstance().getNickname());
            validateDuplication(mTextEmail.getText().toString(), null);
            validateDuplication(null, mTextNickname.getText().toString());
            showFAC();
        }else if(mTextEmail.length() > 0 && mTextNickname.length() > 0){
            validateDuplication(mTextEmail.getText().toString(), null);
            validateDuplication(null, mTextNickname.getText().toString());
        }


        mCompositeSubscription.add(Observable.merge(
            WidgetObservable.text(mTextEmail)
                .debounce(300, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext(event -> {
                    mDuplicatedEmail = false;
                    validateDuplication(event.text().toString(), null);
                    mTextEmail.setError(RxValidator.getErrorMessageEmail.call(event.text().toString()));
                }),
            WidgetObservable.text(mTextNickname)
                .debounce(300, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext(event -> {
                    mDuplicatedNickname = false;
                    validateDuplication(null, event.text().toString());
                    mTextNickname.setError(RxValidator.getErrorMessageNickname.call(event.text().toString()));
                })
            ).subscribe(event -> {
                showFAC();
            }, error -> ErrorHandler.throwError(error, this))
        );

        mCompositeSubscription.add(
            Observable.mergeDelayError(
                FloatingActionControl.clicks().map(event -> FloatingActionControl.getButton().getVisibility() == View.VISIBLE),
                Observable.create(observer -> mTextNickname.setOnEditorActionListener((TextView v, int action, KeyEvent event) -> {
                    observer.onNext(FloatingActionControl.getButton().getVisibility() == View.VISIBLE);
                    return !(FloatingActionControl.getButton().getVisibility() == View.VISIBLE);
                }))
            )
                .filter(use -> use)
                .subscribe(
                    use -> {
                        SignUpForm.getInstance().setEmail(mTextEmail.getText().toString());
                        SignUpForm.getInstance().setNickname(mTextNickname.getText().toString());
                        mViewPagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP3, true);
                    },
                    error -> ErrorHandler.throwError(error, this))
        );
    }

    @Override
    public void onPageUnfocused() {
        if(mCompositeSubscription !=null && !mCompositeSubscription.isUnsubscribed()) mCompositeSubscription.unsubscribe();
    }

    /* For reading contacts to set initial email of user from device */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arguments) {
        return new CursorLoader(getActivity(),
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
        if(emails.size()>0 && emailNotAssigned()) mTextEmail.setText(emails.get(0));
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
}
