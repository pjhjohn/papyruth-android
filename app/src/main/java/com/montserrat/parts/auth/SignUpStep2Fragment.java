package com.montserrat.parts.auth;

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

import com.android.volley.Request;
import com.montserrat.activity.MainActivity;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.controller.AppManager;
import com.montserrat.utils.request.Api;
import com.montserrat.utils.request.ClientFragment;
import com.montserrat.utils.request.RxVolley;
import com.montserrat.utils.validator.RxValidator;
import com.montserrat.utils.validator.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.view.ViewObservable;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep2Fragment extends Fragment {
    @InjectView (R.id.signup_email) protected EditText email;
    @InjectView (R.id.signup_password) protected EditText password;
    @InjectView (R.id.signup_realname) protected EditText realname;
    @InjectView (R.id.signup_nickname) protected EditText nickname;
    @InjectView (R.id.signup_gender) protected RadioGroup genderRadioGroup;
    @InjectView (R.id.signup_entrance) protected Spinner entranceSpinner;
    @InjectView (R.id.submit) protected Button submit;

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

        this.subscriptions.add(Observable
                .combineLatest(
                    WidgetObservable.text(this.email),
                    WidgetObservable.text(this.password),
                    WidgetObservable.text(this.realname),
                    WidgetObservable.text(this.nickname),
                    RxValidator.createObservableRadioGroup(genderRadioGroup),
                    (OnTextChangeEvent email, OnTextChangeEvent password, OnTextChangeEvent realname, OnTextChangeEvent nickname, Integer checked_id) -> {
                        boolean validEmail, validPassword, validRealname, validNickname, validGender;

                        if (validEmail = RxValidator.isValidEmail.call(email))
                            this.email.setError(null);
                        else
                            this.email.setError(this.getResources().getString(RxValidator.isEmpty.call(email) ? R.string.field_invalid_required : R.string.field_invalid_email));

                        if (validPassword = RxValidator.isValidPassword.call(password))
                            this.password.setError(null);
                        else
                            this.password.setError(this.getResources().getString(RxValidator.isEmpty.call(password) ? R.string.field_invalid_required : R.string.field_invalid_password));

                        if (validRealname = RxValidator.isValidRealname.call(password))
                            this.realname.setError(null);
                        else
                            this.realname.setError(this.getResources().getString(RxValidator.isEmpty.call(password) ? R.string.field_invalid_required : R.string.field_invalid_realname));

                        if (validNickname = RxValidator.isValidNickname.call(password))
                            this.password.setError(null);
                        else
                            this.nickname.setError(this.getResources().getString(RxValidator.isEmpty.call(password) ? R.string.field_invalid_required : R.string.field_invalid_nickname));

                        validGender = RxValidator.isValidRadioButton.call(checked_id);

                        return validEmail && validPassword && validRealname && validNickname && validGender;
                    }
                ).subscribe(
                    valid -> {
                        this.submit.getBackground().setColorFilter(getResources().getColor(valid ? R.color.appDefaultHighlightColor : R.color.appDefaultBackgroundColor), PorterDuff.Mode.MULTIPLY);
                        this.submit.setEnabled(valid);
                    }
                )
        );

        this.subscriptions.add(ViewObservable
                .clicks(this.submit)
                .flatMap(unused -> {
                    User.getInstance().setEmail(email.getText().toString());
                    User.getInstance().setRealname(realname.getText().toString());
                    User.getInstance().setNickName(nickname.getText().toString());
                    User.getInstance().setGenderIsBoy(((RadioButton) this.genderRadioGroup.findViewById(genderRadioGroup.getCheckedRadioButtonId())).getText().equals(this.getResources().getString(R.string.gender_male)));
                    User.getInstance().setEntranceYear((Integer) entranceSpinner.getSelectedItem());
                    JSONObject params = User.getInstance().getData();
                    try {
                        params.putOpt("password", this.password.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return RxVolley.createObservable(Api.url("users/sign_up"), Request.Method.POST, params);
                })
                .subscribe(response -> {
                    if (response.optInt("status") == 200 && response.optBoolean("success")) {
                        User.getInstance().setAccessToken(response.optString("access_token", null));
                        AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, response.optString("access_token", null));
                        SignUpStep2Fragment.this.getActivity().startActivity(new Intent(SignUpStep2Fragment.this.getActivity(), MainActivity.class));
                        SignUpStep2Fragment.this.getActivity().finish();
                    }
                })
        );
        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
