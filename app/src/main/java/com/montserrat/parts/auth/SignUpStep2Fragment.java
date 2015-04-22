package com.montserrat.parts.auth;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.montserrat.activity.MainActivity;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.utils.etc.Validator;
import com.montserrat.utils.request.ClientFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

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
        ArrayList<String> list = new ArrayList<String>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for(int year = currentYear; year >= AppConst.MIN_ADMISSION_YEAR; year --) list.add(""+year);
        vAdmission.setAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, list));

        /* button */
        view.findViewById(R.id.btn_signup_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                View viewToFocus = SignUpStep2Fragment.this.validate();
                if (viewToFocus != null) viewToFocus.requestFocus();
                else SignUpStep2Fragment.this.attemptSignUp();
            }
        });
        return view;
    }

    public void attemptSignUp() {
        UserInfo.getInstance().setEmail(vEmail.getText().toString());
        UserInfo.getInstance().setName(vName.getText().toString());
        UserInfo.getInstance().setNickName(vNickname.getText().toString());
        UserInfo.getInstance().setGender(((RadioButton) this.getView().findViewById(vGender.getCheckedRadioButtonId())).getText().equals(getResources().getString(R.string.gender_male)));
//        UserInfo.getInstance().setAdmissionYear();
        if(UserInfo.getInstance().isDataReadyOnStep2()) {
            this.setParameters(null).submit();
        }
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            if (response.getBoolean("success")) this.getActivity().startActivity(new Intent(this.getActivity(), MainActivity.class));
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Validate data in the form and returns if there exist View to be re-checked.
     */
    public View validate() {
        boolean success = false;
        /* email */
        success = Validator.validate(vEmail, Validator.Type.EMAIL, Validator.REQUIRED);
        if(!success) return vEmail;
        /* password */
        success = Validator.validate(vPassword, Validator.Type.PASSWORD, Validator.REQUIRED);
        if(!success) return vPassword;
        /* name */
        success = Validator.validate(vName, Validator.Type.NAME, Validator.REQUIRED);
        if(!success) return vName;
        /* nickname */
        success = Validator.validate(vNickname, Validator.Type.NICKNAME, Validator.REQUIRED);
        if(!success) return vNickname;
        /* gender : TODO - Handle RadioGroup */
        success = Validator.validate(vGender, Validator.REQUIRED);
        if(!success) return vGender;
        /* admission year : TODO - Handle Spinner*/
//        success = Validator.validate(vAdmission, Validator.Type.ADMISSION_YEAR, Validator.REQUIRED);
//        if(!success) return vAdmission;
        /* Initialize Errors to null */

        return null; // Passed all valiation rules
    }

    public void onSignUpSuccess() {
        SignUpStep2Fragment.this.getActivity().startActivity(new Intent(SignUpStep2Fragment.this.getActivity(), MainActivity.class));
    }

    public static Fragment newInstance() {
        Fragment fragment = new SignUpStep2Fragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConst.Request.URL, "pjhjohn.appspot.com");
        bundle.putString(AppConst.Request.CONTROLLER, "university");
        bundle.putString(AppConst.Request.ACTION, "all");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_signup_step2);
        fragment.setArguments(bundle);
        return fragment;
    }
}
