package com.papyruth.android.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.error.SignupError;
import com.papyruth.android.model.unique.SignUpForm;
import com.papyruth.android.model.unique.User;
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
import retrofit.RetrofitError;
import retrofit.mime.TypedByteArray;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep4Fragment extends Fragment implements OnPageFocus, OnPageUnfocus {
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

    @InjectView(R.id.password)      protected EditText mTextPassword;
    @InjectView(R.id.icon_password) protected ImageView mIconPassword;
    @InjectView(R.id.agree_term)    protected TextView mTermOfServicesAgreement;
    private MaterialDialog mTermOfServicesDialog;
    private List<CharSequence> mTermOfServicesStringArguments;
    private CompositeSubscription mCompositeSubscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step4, container, false);
        ButterKnife.inject(this, view);
        mCompositeSubscription = new CompositeSubscription();
        mTermOfServicesStringArguments = new ArrayList<>();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(mCompositeSubscription !=null && !mCompositeSubscription.isUnsubscribed()) mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        Picasso.with(mContext).load(R.drawable.ic_light_lock).transform(new ColorFilterTransformation(mContext.getResources().getColor(R.color.icon_material))).into(mIconPassword);

        if(mViewPagerController.getCurrentPage() == AppConst.ViewPager.Auth.SIGNUP_STEP4){
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mTextPassword, InputMethodManager.SHOW_FORCED);
        }
        mCompositeSubscription.add(Api.papyruth()
            .terms(0)
            .map(terms -> terms.term)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                term -> {
                    if (term != null && term.size() > 0) {
                        mTermOfServicesStringArguments.add(term.get(0).body);
                    } else {
                        mTermOfServicesStringArguments.add("term!" + getResources().getString(R.string.lorem_ipsum));
                        mTermOfServicesStringArguments.add("privacy!!" + getResources().getString(R.string.lorem_ipsum));
                    }
                }, error -> ErrorHandler.throwError(error, this)
            )
        );

        InputMethodManager imm = ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
//        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);

        String term = getString(R.string.term);
        String privacy = getString(R.string.privacy_policy);
        String agreeTermStr = String.format(getString(R.string.agree_terms), term, privacy);

        SpannableString spannableText = new SpannableString(agreeTermStr);

        ClickableSpan termSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                buildTermDialog(0);
                if(!mTermOfServicesDialog.isShowing()) {
                    imm.hideSoftInputFromWindow(mTextPassword.getWindowToken(), 0);
                    mTermOfServicesDialog.show();
                }
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        ClickableSpan privacySpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                buildTermDialog(1);
                if(!mTermOfServicesDialog.isShowing()) {
                    imm.hideSoftInputFromWindow(mTextPassword.getWindowToken(), 0);
                    mTermOfServicesDialog.show();
                }
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        int wordIndex = agreeTermStr.indexOf(term);
        spannableText.setSpan(termSpan, wordIndex, wordIndex+term.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        wordIndex = agreeTermStr.indexOf(privacy);
        spannableText.setSpan(privacySpan, wordIndex, wordIndex+privacy.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mTermOfServicesAgreement.setText(spannableText);
        mTermOfServicesAgreement.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void showFAC() {
        String validatePassword = RxValidator.getErrorMessagePassword.call(mTextPassword.getText().toString());

        boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
        boolean valid = validatePassword == null;

        if (!visible && valid) FloatingActionControl.getInstance().show(true, 200, TimeUnit.MILLISECONDS);
        else if (visible && !valid) FloatingActionControl.getInstance().hide(true);
    }

    private void buildTermDialog(int number){
        String title = getString(R.string.term);
        if(number == 0)
            title = getString(R.string.term);
        else if(number == 1)
            title = getString(R.string.privacy_policy);
        mTermOfServicesDialog = new MaterialDialog.Builder(getActivity())
            .title(title)
            .content(mTermOfServicesStringArguments.get(number))
            .positiveText(R.string.agree)
            .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);
                }
            })
            .build();
    }

    @Override
    public void onPageFocused() {
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_auth_signup4));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_done).hide(true);
        ((AuthActivity) getActivity()).setOnShowSoftKeyboard(null);
        ((AuthActivity) getActivity()).setOnHideSoftKeyboard(null);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        mTextPassword.requestFocus();

        if(mCompositeSubscription != null && mCompositeSubscription.isUnsubscribed()) mCompositeSubscription = new CompositeSubscription();

        if(SignUpForm.getInstance().getPassword() != null) mTextPassword.setText(SignUpForm.getInstance().getPassword());
        if(mTextPassword != null) showFAC();

        mCompositeSubscription.add(WidgetObservable
            .text(mTextPassword)
            .debounce(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe(event -> {
                String validatePassword = RxValidator.getErrorMessagePassword.call(event.text().toString());
                mTextPassword.setError(validatePassword);
                showFAC();
            }, error -> ErrorHandler.throwError(error, this))
        );

        mCompositeSubscription.add(Observable
            .mergeDelayError(
                FloatingActionControl.clicks().map(event -> FloatingActionControl.getButton().getVisibility() == View.VISIBLE),
                Observable.create(observer -> mTextPassword.setOnEditorActionListener((TextView v, int action, KeyEvent event) -> {
                    observer.onNext(FloatingActionControl.getButton().getVisibility() == View.VISIBLE);
                    return !(FloatingActionControl.getButton().getVisibility() == View.VISIBLE);
                }))
            )
            .filter(use -> use)
            .subscribe(
                use -> {
                    SignUpForm.getInstance().setPassword(mTextPassword.getText().toString());
                    submitRegistration();
                }, error -> ErrorHandler.throwError(error, this)
            )
        );
    }


    private void submitRegistration() {
        mCompositeSubscription.add(Api.papyruth()
            .users_sign_up(
                SignUpForm.getInstance().getEmail(),
                SignUpForm.getInstance().getPassword(),
                SignUpForm.getInstance().getRealname(),
                SignUpForm.getInstance().getNickname(),
                SignUpForm.getInstance().getIsBoy(),
                SignUpForm.getInstance().getUniversityId(),
                SignUpForm.getInstance().getEntranceYear()
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    if (response.success) {
                        User.getInstance().update(response.user, response.access_token);
                        AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, response.access_token);
                        SignUpForm.getInstance().clear();
                        getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
                        getActivity().finish();
                    } else
                        Toast.makeText(getActivity(), getResources().getString(R.string.failed_sign_in), Toast.LENGTH_LONG).show();
                },
                error -> {
                    if (error instanceof RetrofitError) {
                        switch (((RetrofitError) error).getResponse().getStatus()) {
                            case 400: // Invalid field or lack of required field.
                                String json = new String(((TypedByteArray) ((RetrofitError) error).getResponse().getBody()).getBytes());
                                Gson gson = new Gson();
                                Timber.d("reason : %s", gson.fromJson(json, SignupError.class).errors.email);
                            case 403: // Failed to SignUp
                                Toast.makeText(getActivity(), getResources().getString(R.string.failed_sign_up), Toast.LENGTH_LONG).show();
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
    public void onPageUnfocused() {
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }
}
