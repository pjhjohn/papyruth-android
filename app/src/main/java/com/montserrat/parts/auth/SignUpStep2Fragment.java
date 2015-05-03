package com.montserrat.parts.auth;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.montserrat.activity.MainActivity;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.controller.AppManager;
import com.montserrat.utils.request.ClientFragment;
import com.montserrat.utils.validator.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep2Fragment extends ClientFragment {
    private EditText vEmail, vPassword, vName, vNickname;
    private RadioGroup vGender;
    private Spinner vAdmission;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        /* email */
        vEmail = (EditText) view.findViewById(R.id.signup_email);
        /* password */
        vPassword = (EditText) view.findViewById(R.id.signup_password);
        /* name */
        vName = (EditText) view.findViewById(R.id.signup_name);
        /* nickname */
        vNickname = (EditText) view.findViewById(R.id.signup_nickname);
        /* gender */
        vGender = (RadioGroup) view.findViewById(R.id.signup_gender);

        /* admission year */
        vAdmission = (Spinner) view.findViewById(R.id.signup_admission);
        ArrayList<Integer> list = new ArrayList<Integer>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for(int year = currentYear; year >= AppConst.MIN_ADMISSION_YEAR; year --) list.add(year);
        vAdmission.setAdapter(new ArrayAdapter<Integer>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, list));

        /* button */
        view.findViewById(R.id.btn_signup_submit).setOnClickListener(v -> {
            View viewToFocus = this.validate();
            if (viewToFocus != null) viewToFocus.requestFocus();
            else this.attemptSignUp();
        });
        return view;
    }

    public void attemptSignUp() {
        UserInfo.getInstance().setEmail(vEmail.getText().toString());
        UserInfo.getInstance().setRealname(vName.getText().toString());
        UserInfo.getInstance().setNickName(vNickname.getText().toString());
        UserInfo.getInstance().setGenderIsBoy(((RadioButton) this.getView().findViewById(vGender.getCheckedRadioButtonId())).getText().equals(getResources().getString(R.string.gender_male)));
        UserInfo.getInstance().setAdmissionYear((Integer)vAdmission.getSelectedItem());
        if ( UserInfo.getInstance().getCompletionLevel() >= 2 ) {
            JSONObject data = UserInfo.getInstance().getData();
            try {
                data.put("password", this.vPassword.getText().toString());
            } catch(JSONException e) {
                e.printStackTrace();
            }
            this.setParameters(data).submit();
        }
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            if (response.getBoolean("success")) {
                UserInfo.getInstance().setAccessToken(response.optString("access_token", null)); // TODO : Check existance of access-token at setter or
                AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, response.optString("access_token", null));
                this.onSignUpSuccess();
            }
            else this.onSignUpFailure(response);
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Validate data in the form and returns if there exist View to be re-checked.
     */
    public View validate() {
        List<View> vFailed = new ArrayList<View>();
        View candidate;
        /* email */
        candidate = Validator.validate(vEmail, Validator.TextType.EMAIL, Validator.REQUIRED);
        if(candidate != null) vFailed.add(candidate);
        /* password - TODO : Should handle PASSWORD ENCRYPTION */
        candidate = Validator.validate(vPassword, Validator.TextType.PASSWORD, Validator.REQUIRED);
        if(candidate != null) vFailed.add(candidate);
        /* name */
        candidate = Validator.validate(vName, Validator.TextType.NAME, Validator.REQUIRED);
        if(candidate != null) vFailed.add(candidate);
        /* nickname */
        candidate = Validator.validate(vNickname, Validator.TextType.NICKNAME, Validator.REQUIRED);
        if(candidate != null) vFailed.add(candidate);
        /* gender */
        candidate = Validator.validate(vGender, Validator.REQUIRED);
        if(candidate != null) vFailed.add(candidate);
        /* admission year */
        candidate = Validator.validate(vAdmission, Validator.SpinnerType.ADMISSION, Validator.REQUIRED);
        if(candidate != null) vFailed.add(candidate);
        /* Initialize Errors to null */

        return vFailed.isEmpty() ? null : vFailed.get(0);
    }

    public void onSignUpSuccess() {
        SignUpStep2Fragment.this.getActivity().startActivity(new Intent(SignUpStep2Fragment.this.getActivity(), MainActivity.class));
		SignUpStep2Fragment.this.getActivity().finish();
    }
    public void onSignUpFailure(JSONObject response) {
        Toast.makeText(this.getActivity(), response.toString(), Toast.LENGTH_LONG).show();
    }

    public static Fragment newInstance() {
        Fragment fragment = new SignUpStep2Fragment();
        Bundle bundle = new Bundle();
        bundle.putInt(AppConst.Request.METHOD, AppConst.Request.Method.POST);
        bundle.putString(AppConst.Request.API_ROOT_URL, AppConst.API_ROOT);
        bundle.putString(AppConst.Request.API_VERSION, AppConst.API_VERSION);
        bundle.putString(AppConst.Request.ACTION, "users/sign_up");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_signup_step2);
        fragment.setArguments(bundle);
        return fragment;
    }
}
