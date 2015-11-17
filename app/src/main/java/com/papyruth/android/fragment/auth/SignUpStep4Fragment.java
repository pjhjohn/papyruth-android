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
import com.google.gson.Gson;
import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.error.SignupError;
import com.papyruth.android.model.unique.SignUpForm;
import com.papyruth.android.model.unique.User;
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

    @InjectView(R.id.password) protected EditText password;
    @InjectView(R.id.icon_password) protected ImageView iconPassword;
    @InjectView(R.id.agree_term) protected TextView agreeTerm;
    private MaterialDialog termPage;
    private List<CharSequence> termContents;
    private CompositeSubscription subscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step4, container, false);
        ButterKnife.inject(this, view);
        this.subscription = new CompositeSubscription();
        this.termContents = new ArrayList<>();
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
        Picasso.with(this.getActivity().getBaseContext()).load(R.drawable.ic_light_lock).transform(new ColorFilterTransformation(this.getResources().getColor(R.color.primary_dark_material_dark))).into(this.iconPassword);

        if(this.pagerController.getCurrentPage() == AppConst.ViewPager.Auth.SIGNUP_STEP4){
            ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(this.password, InputMethodManager.SHOW_FORCED);
        }
        this.subscription.add(
            Api.papyruth().terms(0)
                .map(terms -> terms.term)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    term -> {
                        if (term != null && term.size() > 0) {
                            this.termContents.add(term.get(0).body);
                        }else {
                            this.termContents.add("term!"+this.getResources().getString(R.string.lorem_ipsum));
                            this.termContents.add("privacy!!"+this.getResources().getString(R.string.lorem_ipsum));
                        }
                    }, error -> {
                        Timber.d("get Term error", error);
                        error.printStackTrace();
                    }
                )
        );

        InputMethodManager imm = ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
//        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);

        String term = getString(R.string.term);
        String privacy = getString(R.string.privacy_policy);
        String agreeTerm = String.format(getString(R.string.agree_terms), term, privacy);

        SpannableString spannableText = new SpannableString(agreeTerm);

        ClickableSpan termSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                buildTermDialog(0);
                if(!termPage.isShowing()) {
                    imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
                    termPage.show();
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
                if(!termPage.isShowing()) {
                    imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
                    termPage.show();
                }
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        int wordIndex = agreeTerm.indexOf(term);
        spannableText.setSpan(termSpan, wordIndex, wordIndex+term.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        wordIndex = agreeTerm.indexOf(privacy);
        spannableText.setSpan(privacySpan, wordIndex, wordIndex+privacy.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        this.agreeTerm.setText(spannableText);
        this.agreeTerm.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void showFAC() {
        String validatePassword = RxValidator.getErrorMessagePassword.call(this.password.getText().toString());

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
        this.termPage = new MaterialDialog.Builder(this.getActivity())
            .title(title)
            .content(this.termContents.get(number))
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
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_done).hide(true);
        ((AuthActivity) getActivity()).setOnShowSoftKeyboard(null);
        ((AuthActivity) getActivity()).setOnHideSoftKeyboard(null);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        this.password.requestFocus();

        if(this.subscription.isUnsubscribed())
            this.subscription = new CompositeSubscription();

        if(SignUpForm.getInstance().getPassword() != null){
            this.password.setText(SignUpForm.getInstance().getPassword());
            this.showFAC();
        }else if(this.password.length() > 0){
            this.showFAC();
        }
        this.subscription.add(WidgetObservable
            .text(this.password)
            .debounce(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe(event -> {
                String validatePassword = RxValidator.getErrorMessagePassword.call(event.text().toString());
                this.password.setError(validatePassword);
                this.showFAC();
            })
        );

        this.subscription.add(
            Observable.mergeDelayError(
                FloatingActionControl.clicks().map(event -> FloatingActionControl.getButton().getVisibility() == View.VISIBLE),
                Observable.create(observer -> this.password.setOnEditorActionListener((TextView v, int action, KeyEvent event) -> {
                    observer.onNext(FloatingActionControl.getButton().getVisibility() == View.VISIBLE);
                    return !(FloatingActionControl.getButton().getVisibility() == View.VISIBLE);
                }))
            )
                .filter(use -> use)
                .subscribe(
                    use -> {
                        SignUpForm.getInstance().setPassword(this.password.getText().toString());
                        this.register();
                    },
                    error -> error.printStackTrace())
        );
    }


    private void register(){
        this.subscription.add(
            Api.papyruth().users_sign_up(
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
                        SignUpStep4Fragment.this.getActivity().startActivity(new Intent(SignUpStep4Fragment.this.getActivity(), MainActivity.class));
                        SignUpStep4Fragment.this.getActivity().finish();
                    } else {
                        Toast.makeText(this.getActivity(), this.getResources().getString(R.string.failed_sign_in), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    if (error instanceof RetrofitError) {
                        switch (((RetrofitError) error).getResponse().getStatus()) {
                            case 400: // Invalid field or lack of required field.
                                String json = new String(((TypedByteArray)((RetrofitError)error).getResponse().getBody()).getBytes());
                                Gson gson = new Gson();
                                Timber.d("reason : %s", gson.fromJson(json, SignupError.class).errors.email);
                            case 403: // Failed to SignUp
                                Toast.makeText(this.getActivity(), this.getResources().getString(R.string.failed_sign_up), Toast.LENGTH_LONG).show();
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
        if(this.subscription !=null && !this.subscription.isUnsubscribed()) this.subscription.unsubscribe();
    }
}
