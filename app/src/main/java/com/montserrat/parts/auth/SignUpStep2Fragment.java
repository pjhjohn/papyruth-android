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
import com.montserrat.utils.request.RxVolley;
import com.montserrat.utils.validator.RxValidator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.android.widget.WidgetObservable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.montserrat.utils.validator.RxValidator.isValidRadioButton;
import static com.montserrat.utils.validator.RxValidator.toString;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep2Fragment extends Fragment {
    @InjectView (R.id.email) protected EditText email;
    @InjectView (R.id.password) protected EditText password;
    @InjectView (R.id.realname) protected EditText realname;
    @InjectView (R.id.nickname) protected EditText nickname;
    @InjectView (R.id.gender) protected RadioGroup genderRadioGroup;
    @InjectView (R.id.entrance) protected Spinner entranceSpinner;
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

        this.subscriptions.add( Observable.combineLatest(
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
                this.submit.getBackground().setColorFilter(getResources().getColor(valid ? R.color.appDefaultHighlightColor : R.color.appDefaultBackgroundColor), PorterDuff.Mode.MULTIPLY);
                this.submit.setEnabled(valid);
            })
        );

        this.subscriptions.add( ViewObservable
            .clicks(this.submit)
            .flatMap( unused -> {
                User.getInstance().setEmail(email.getText().toString());
                User.getInstance().setRealname(realname.getText().toString());
                User.getInstance().setNickName(nickname.getText().toString());
                User.getInstance().setGenderIsBoy(((RadioButton) this.genderRadioGroup.findViewById(genderRadioGroup.getCheckedRadioButtonId())).getText().equals(this.getResources().getString(R.string.gender_male)));
                User.getInstance().setEntranceYear((Integer) entranceSpinner.getSelectedItem());
                JSONObject params = User.getInstance().getData();
                try { params.putOpt("password", this.password.getText().toString()); }
                catch (JSONException e) { e.printStackTrace(); }
                return RxVolley.createObservable(Api.url("users/sign_up"), Request.Method.POST, params);
            })
            .subscribe(response -> {
                switch (response.optInt("status")) {
                    case 200:
                        if (response.optBoolean("success")) {
                            User.getInstance().setAccessToken(response.optString("access_token", null));
                            AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, response.optString("access_token", null));
                            SignUpStep2Fragment.this.getActivity().startActivity(new Intent(SignUpStep2Fragment.this.getActivity(), MainActivity.class));
                            SignUpStep2Fragment.this.getActivity().finish();
                        } else {
                            // TODO : Failed to Signup - Email Duplication or something...
                        } break;
                    default : Timber.e("Unexpected Status code : %d - Needs to be implemented", response.optInt("status"));
                }
            })
        );
        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }
}
