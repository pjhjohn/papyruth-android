package com.montserrat.parts.auth;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.montserrat.activity.MainActivity;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.utils.validator.Validator;
import com.montserrat.utils.request.ClientFragment;
import com.montserrat.utils.viewpager.ViewPagerController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mrl on 2015-04-07.
 */
public class AuthFragment extends ClientFragment {
    public AuthFragment (){}

    private AutoCompleteTextView vEmail;
    private EditText vPassword;
    private ViewPagerController pageController;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pageController = (ViewPagerController) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        this.vEmail = (AutoCompleteTextView) view.findViewById(R.id.auth_email);
        this.getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(
                        AuthFragment.this.getActivity(),
                        Uri.withAppendedPath(
                                ContactsContract.Profile.CONTENT_URI,
                                ContactsContract.Contacts.Data.CONTENT_DIRECTORY
                        ),
                        ProfileQuery.PROJECTION,
                        ContactsContract.Contacts.Data.MIMETYPE + " = ?",
                        new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE},
                        ContactsContract.Contacts.Data.IS_PRIMARY + " DESC"
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
                List<String> emails = new ArrayList<String>();
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    emails.add(cursor.getString(ProfileQuery.ADDRESS));
                    cursor.moveToNext();
                }
                /* Add to Emails View */
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        AuthFragment.this.getActivity(),
                        android.R.layout.simple_dropdown_item_1line,
                        emails
                );
                vEmail.setAdapter(adapter);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> cursorLoader) {}
        });

        this.vPassword = (EditText) view.findViewById(R.id.auth_password);
        this.vPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.id.signin || actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE) {
                    AuthFragment.this.attemtSignin();
                    return true;
                } else return false;
            }
        });

        view.findViewById(R.id.btn_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthFragment.this.attemtSignin();
            }
        });

        view.findViewById(R.id.btn_sign_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthFragment.this.attemptSignup();
            }
        });

        return view;
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };
        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void attemtSignin() {
        List<View> vFailed = new ArrayList<View>();
        View candidate;
        /* email */
        candidate = Validator.validate(vEmail, Validator.TextType.EMAIL, Validator.REQUIRED);
        if(candidate != null) vFailed.add(candidate);
        /* password - TODO : Should handle PASSWORD ENCRYPTION */
        candidate = Validator.validate(vPassword, Validator.TextType.PASSWORD, Validator.REQUIRED);
        if(candidate != null) vFailed.add(candidate);

        if(vFailed.isEmpty()) {
            try {
                this.setParameters(new JSONObject().put("email", vEmail.getText().toString()).put("password", vPassword.getText().toString()))
                        .submit();
            } catch (JSONException e){
                e.printStackTrace();
            }
        } else vFailed.get(0).requestFocus();
    }

    @Override
    public void onResponse(JSONObject resp) {
        super.onResponse(resp);
        try {
            if(resp.getBoolean("success")) this.getActivity().startActivity(new Intent(AuthFragment.this.getActivity(), MainActivity.class));
            else {
                this.vPassword.setError("Invalid Password");
                this.vPassword.requestFocus();
            }

        } catch(JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        Toast.makeText(AuthFragment.this.getActivity(), error.toString(), Toast.LENGTH_LONG).show();
    }

    // TODO : Replace this with your own logic
    private void attemptSignup() {
        this.pageController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP1, true);
    }

    public static Fragment newInstance() {
        Fragment fragment = new AuthFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConst.Request.URL, "pjhjohn.appspot.com");
        bundle.putString(AppConst.Request.CONTROLLER, "signin");
        bundle.putString(AppConst.Request.ACTION, "validate");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_auth);
        bundle.putInt(AppConst.Resource.PROGRESS, R.id.progress_auth);
        bundle.putInt(AppConst.Resource.CONTENT, R.id.content_auth);
        fragment.setArguments(bundle);
        return fragment;
    }
}
