package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.views.ButtonFlat;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.activity.MainActivity;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.support.rx.RxValidator;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;
import com.rengwuxian.materialedittext.MaterialEditText;

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

import static com.montserrat.utils.support.rx.RxValidator.isValidRadioButton;
import static com.montserrat.utils.support.rx.RxValidator.toString;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep1FragmentOld extends Fragment implements OnPageFocus {
    private ViewPagerController pagerController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    @InjectView (R.id.email) protected MaterialEditText email;
    @InjectView (R.id.password) protected MaterialEditText password;
    @InjectView (R.id.realname) protected MaterialEditText realname;
    @InjectView (R.id.nickname) protected MaterialEditText nickname;
    @InjectView (R.id.gender) protected RadioGroup genderRadioGroup;
    @InjectView (R.id.university) protected ButtonFlat university;
    @InjectView (R.id.entrance) protected ButtonFlat entrance;
    @InjectView (R.id.progress) protected View progress;
    private CompositeSubscription subscriptions;
    private Integer entranceYear;
    private MaterialDialog entranceYearDialog;
    private Observable<Integer> entranceYearObservable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.entranceYear = null;
        this.university.setRippleSpeed(50.0f);
        this.entrance.setRippleSpeed(50.0f);
        this.entranceYearObservable = this.buildEntranceYearDialog();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    private void register () {
        this.progress.setVisibility(View.VISIBLE);
        this.subscriptions.add(
            RetrofitApi.getInstance().users_sign_up(
                this.email.getText().toString(),
                this.password.getText().toString(),
                this.realname.getText().toString(),
                this.nickname.getText().toString(),
                ((RadioButton) this.genderRadioGroup.findViewById(genderRadioGroup.getCheckedRadioButtonId())).getText().equals(this.getResources().getString(R.string.gender_male)),
                User.getInstance().getUniversityId(),
                this.entranceYear
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    this.progress.setVisibility(View.GONE);
                    if (response.success) {
                        User.getInstance().update(response.user, response.access_token);
                        AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, response.access_token);
                        SignUpStep1FragmentOld.this.getActivity().startActivity(new Intent(SignUpStep1FragmentOld.this.getActivity(), MainActivity.class));
                        SignUpStep1FragmentOld.this.getActivity().finish();
                    } else {
                        Toast.makeText(this.getActivity(), this.getResources().getString(R.string.failed_sign_in), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    this.progress.setVisibility(View.GONE);
                    if (error instanceof RetrofitError) {
                        switch (((RetrofitError) error).getResponse().getStatus()) {
                            case 400: // Invalid field or lack of required field.
                            case 403: // Failed to SignUp
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
    public void onPageFocused () {
        FloatingActionControl.getInstance().setControl(R.layout.fab_done);
        this.university.setText(User.getInstance().getUniversityName());

        this.subscriptions.add(ViewObservable.clicks(this.entrance).filter(unused -> !this.entranceYearDialog.isShowing()).subscribe(unused -> this.entranceYearDialog.show()));
        this.subscriptions.add(Observable.combineLatest(
            WidgetObservable.text(this.email).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessageEmail),
            WidgetObservable.text(this.password).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessagePassword),
            WidgetObservable.text(this.realname).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessageRealname),
            WidgetObservable.text(this.nickname).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessageNickname),
            RxValidator.createObservableRadioGroup(genderRadioGroup).map(isValidRadioButton),
            this.entranceYearObservable.startWith((Integer) null),
            (String emailError, String passwordError, String realnameError, String nicknameError, Boolean validRadioGroup, Integer entranceYear) -> {
                this.email.setError(emailError);
                this.password.setError(passwordError);
                this.realname.setError(realnameError);
                this.nickname.setError(nicknameError);
                return emailError == null && passwordError == null && realnameError == null && nicknameError == null &&
                    validRadioGroup != null && validRadioGroup &&
                    entranceYear != null && AppConst.MIN_ENTRANCE_YEAR <= entranceYear && entranceYear <= Calendar.getInstance().get(Calendar.YEAR);
            })
            .startWith(false)
            .subscribe(valid -> {
                boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                if (visible && !valid) FloatingActionControl.getInstance().hide(true);
                else if (!visible && valid) FloatingActionControl.getInstance().show(true);
            })
        );

        this.subscriptions.add(FloatingActionControl.clicks().subscribe(unused ->
            new MaterialDialog.Builder(this.getActivity())
                .title(R.string.dialog_title_confirm_submit)
                .content(R.string.dialog_content_configm_submit)
                .positiveText(R.string.submit)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        SignUpStep1FragmentOld.this.register();
                    }
                })
                .show()
        ));
        this.subscriptions.add(ViewObservable.clicks(this.university).subscribe(unused -> pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_UNIV, true)));
    }

    private Observable<Integer> buildEntranceYearDialog() {
        final int length = Calendar.getInstance().get(Calendar.YEAR) - AppConst.MIN_ENTRANCE_YEAR + 1;
        String[] years = new String[length];
        for(int i = 0; i < length; i ++) years[i] = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - i);
        return Observable.create(observer -> this.entranceYearDialog = new MaterialDialog.Builder(this.getActivity())
            .title(R.string.dialog_title_entrance_year)
            .negativeText(R.string.cancel)
            .items(years)
            .itemsCallback((dialog, view, which, text) -> {
                this.entrance.setText(text.toString() + getResources().getString(R.string.entrance_postfix));
                this.entranceYear = Integer.parseInt(text.toString());
                observer.onNext(this.entranceYear);
            })
            .build()
        );
    }
}
