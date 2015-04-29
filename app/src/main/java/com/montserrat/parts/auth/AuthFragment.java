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

import com.android.volley.VolleyError;
import com.montserrat.activity.MainActivity;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.utils.request.ClientFragment;
import com.montserrat.utils.validator.Validator;
import com.montserrat.utils.viewpager.ViewPagerController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mrl on 2015-04-07.
 */
public class AuthFragment extends ClientFragment {
    private AutoCompleteTextView vEmail;
    private EditText vPassword;
    private ViewPagerController pagerController;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        /* Bind Views */
        this.vEmail = (AutoCompleteTextView) view.findViewById(R.id.auth_email);

        /* AutoComplete Email View */
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
                        new String[] { ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE },
                        ContactsContract.Contacts.Data.IS_PRIMARY + " DESC"
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
                List<String> emails = new ArrayList<>();
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    emails.add(cursor.getString(ProfileQuery.ADDRESS));
                    cursor.moveToNext();
                }
                /* Add to Emails View */
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        AuthFragment.this.getActivity(),
                        android.R.layout.simple_dropdown_item_1line,
                        emails
                );
                vEmail.setAdapter(adapter);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> cursorLoader) {}
        });

        /* Password View */
        this.vPassword = (EditText) view.findViewById(R.id.auth_password);
        this.vPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int action, KeyEvent event) {
                if (action == R.id.signin || action == EditorInfo.IME_NULL || action == EditorInfo.IME_ACTION_DONE) {
                    AuthFragment.this.attemtSignin();
                    return true;
                } else return false;
            }
        });

        /* Signin Button */
        view.findViewById(R.id.btn_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthFragment.this.attemtSignin();
            }
        });

        /* Signup Button*/
        view.findViewById(R.id.btn_sign_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthFragment.this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP1, true);
            }
        });

        return view;
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
        /* password : plain password through HTTPS */
        candidate = Validator.validate(vPassword, Validator.TextType.PASSWORD, Validator.REQUIRED);
        if(candidate != null) vFailed.add(candidate);

        /* Failed to pass validator : RequestFocus to first invalid view */
        if(!vFailed.isEmpty()) {
            vFailed.get(0).requestFocus();
            return;
        }

        /* Success to pass validator : Send request */
        JSONObject params = new JSONObject();
        try {
            params.put("email", vEmail.getText().toString())
                  .put("password", vPassword.getText().toString());
        } catch (JSONException e){
            params = new JSONObject();
        } this.setParameters(params).submit();
    }

    @Override
    public void onResponse(JSONObject resp) {
        super.onResponse(resp);
        boolean success = resp.optBoolean("success", false);
        if (!success) this.onFailedToSignin();
        else {
            UserInfo.getInstance().setData(resp.optJSONObject("user"));
            UserInfo.getInstance().setAccessToken(resp.optString(null)); // TODO : Check existance of access-token at setter or
            this.getActivity().startActivity(new Intent(AuthFragment.this.getActivity(), MainActivity.class));
            this.getActivity().finish();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        /* 403 if failed to signin */
        if(error.networkResponse.statusCode == 403) {
            this.onFailedToSignin();
        }
    }

    private void onFailedToSignin() {
        this.vPassword.setError("Invalid Password");
        this.vPassword.requestFocus();
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };
        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    public static Fragment newInstance() {
        Fragment fragment = new AuthFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(AppConst.Request.METHOD, AppConst.Request.Method.POST);
        bundle.putString(AppConst.Request.API_ROOT_URL, AppConst.API_ROOT);
        bundle.putString(AppConst.Request.API_VERSION, AppConst.API_VERSION);
        bundle.putString(AppConst.Request.ACTION, "users/sign_in");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_auth);
        bundle.putInt(AppConst.Resource.PROGRESS, R.id.progress_auth);
        bundle.putInt(AppConst.Resource.CONTENT, R.id.content_auth);
        fragment.setArguments(bundle);
        return fragment;
    }
}
