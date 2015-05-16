package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.activity.MainActivity;
import com.montserrat.app.model.User;
import com.montserrat.utils.etc.RetrofitApi;
import com.montserrat.utils.validator.RxValidator;
import com.montserrat.utils.viewpager.OnPageFocus;
import com.montserrat.utils.viewpager.ViewPagerController;

import java.util.ArrayList;
import java.util.Calendar;
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

import static com.montserrat.utils.validator.RxValidator.isValidRadioButton;
import static com.montserrat.utils.validator.RxValidator.toString;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep2Fragment extends Fragment implements OnPageFocus {
    private ViewPagerController pageController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pageController = (ViewPagerController) activity;
    }

    @InjectView (R.id.university) protected Button university;
    @InjectView (R.id.email) protected EditText email;
    @InjectView (R.id.password) protected EditText password;
    @InjectView (R.id.realname) protected EditText realname;
    @InjectView (R.id.nickname) protected EditText nickname;
    @InjectView (R.id.gender) protected RadioGroup genderRadioGroup;
    @InjectView (R.id.entrance) protected Spinner entranceSpinner;
    @InjectView (R.id.submit) protected Button submit;
    @InjectView (R.id.progress) protected View progress;
    private CompositeSubscription subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step2, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();

        /* set entrance-year spinner */
        ArrayList<Integer> list = new ArrayList<>();
        int todayear = Calendar.getInstance().get(Calendar.YEAR);
        for(int year = todayear; year >= AppConst.MIN_ADMISSION_YEAR; year --) list.add(year);
        entranceSpinner.setAdapter(new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, list));

        this.subscriptions.add(Observable.combineLatest(
            WidgetObservable.text(this.email).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessageEmail),
            WidgetObservable.text(this.password).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessagePassword),
            WidgetObservable.text(this.realname).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessageRealname),
            WidgetObservable.text(this.nickname).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessageNickname),
            RxValidator.createObservableRadioGroup(genderRadioGroup).map(isValidRadioButton),
            (String emailError, String passwordError, String realnameError, String nicknameError, Boolean validRadioGroup) -> {
                this.email.setError(emailError);
                this.password.setError(passwordError);
                this.realname.setError(realnameError);
                this.nickname.setError(nicknameError);
                return emailError == null && passwordError == null && realnameError == null && nicknameError == null;
            })
            .startWith(false)
            .subscribe(valid -> {
                this.submit.getBackground().setColorFilter(getResources().getColor(valid ? R.color.fg_accent : R.color.transparent), PorterDuff.Mode.MULTIPLY);
                this.submit.setEnabled(valid);
            })
        );

        this.subscriptions.add(ViewObservable.clicks(this.submit).subscribe(unused -> register()));
        this.subscriptions.add(ViewObservable.clicks(this.university).subscribe(unused ->pageController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP1, true)));
        return view;
    }

    private void register () {
        this.progress.setVisibility(View.VISIBLE);
        this.subscriptions.add(
            RetrofitApi.getInstance().signup(
                this.email.getText().toString(),
                this.password.getText().toString(),
                this.realname.getText().toString(),
                this.nickname.getText().toString(),
                ((RadioButton) this.genderRadioGroup.findViewById(genderRadioGroup.getCheckedRadioButtonId())).getText().equals(this.getResources().getString(R.string.gender_male)),
                User.getInstance().getUniversityId(),
                (Integer) entranceSpinner.getSelectedItem()
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    this.progress.setVisibility(View.GONE);
                    if (response.success) {
                        User.getInstance().update(response.user, response.access_token);
                        AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, response.access_token);
                        SignUpStep2Fragment.this.getActivity().startActivity(new Intent(SignUpStep2Fragment.this.getActivity(), MainActivity.class));
                        SignUpStep2Fragment.this.getActivity().finish();
                    } else {
                        // TODO : Failed to Signup - Email Duplication or something...
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

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    public void onPageFocused () {
        this.university.setText(User.getInstance().getUniversityName());
    }
}
