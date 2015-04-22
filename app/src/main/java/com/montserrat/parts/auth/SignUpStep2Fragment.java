package com.montserrat.parts.auth;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.montserrat.activity.MainActivity;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.utils.request.ClientFragment;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep2Fragment extends ClientFragment implements View.OnClickListener{
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
        view.findViewById(R.id.btn_signup_submit).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
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
