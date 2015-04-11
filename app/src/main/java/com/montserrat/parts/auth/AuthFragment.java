package com.montserrat.parts.auth;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
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

import com.montserrat.activity.MainActivity;
import com.montserrat.activity.R;
import com.montserrat.utils.requestable_fragment.JSONRequestableFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mrl on 2015-04-07.
 */
public class AuthFragment extends JSONRequestableFragment{
    public AuthFragment (){}

    private AutoCompleteTextView vEmail;
    private EditText vPassword;

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
        this.form.clear();

        vEmail.setError(null);
        vPassword.setError(null);

        String email = vEmail.getText().toString();
        String passwd = vPassword.getText().toString();

        boolean cancel = false;
        View vFocus = null;

        /* Client-side Form Validation */
        if (!TextUtils.isEmpty(passwd) && !isPasswordValid(passwd)) {
            vPassword.setError("This password is too short");
            vFocus = vPassword;
            cancel = true;
        }
        if (TextUtils.isEmpty(email)) {
            vEmail.setError("This field is required");
            vFocus = vEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            vEmail.setError("This email address is invalid");
            vFocus = vEmail;
            cancel = true;
        }

        if (cancel) {
            vFocus.requestFocus();
        } else {
            try {
                this.form.put("email", email)
                         .put("password", passwd)
                         .submit();
            } catch (JSONException | UnsupportedEncodingException e) {
                Log.e (this.getClass().toString(), e.toString());
            }
        }
    }

    @Override protected String getEndpoint() {
        return "http://pjhjohn.appspot.com/signin";
    }
    @Override protected int getFragmentLayoutId() {
        return R.layout.fragment_auth;
    }
    @Override protected int getProgressViewId() {
        return R.id.auth_progress;
    }
    @Override protected int getContentViewId() {
        return R.id.auth_content;
    }

    @Override
    public void onSuccess(String responseBody) {
        super.onSuccess(responseBody);
        JSONObject json = null;
        try {
            json = new JSONObject(responseBody);
        } catch (JSONException e) {
            Toast.makeText(this.getActivity(), "Exception During Parsing : " + responseBody, Toast.LENGTH_LONG).show();
        }
        if(json == null) return;
        else {
            try {
                if (json.getBoolean("success")) {
                    AuthFragment.this.getActivity().finish();
                } else {
                    this.vPassword.setError("Invalid Password");
                    this.vPassword.requestFocus();
                }
            } catch (JSONException e) {
                Toast.makeText(this.getActivity(), "Exception During getting boolean" + responseBody, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onTimeout(String errorMsg) {
        super.onTimeout(errorMsg);
        Toast.makeText(this.getActivity(), "인터넷 연결이 불안정합니다.", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onNoInternetConnection(String errorMsg) {
        super.onNoInternetConnection(errorMsg);
        Toast.makeText(this.getActivity(), "인터넷 연결이 되어있지 않습니다.", Toast.LENGTH_LONG).show();
    }

    // TODO : Replace this with your own logic
    private boolean isEmailValid(String email) {
        return email.contains("@");
    }
    private boolean isPasswordValid(String passwd) {
        return passwd.length() > 4;
    }
    private void attemptSignup() {
        this.startActivity(new Intent(this.getActivity(), MainActivity.class));
    }
}
