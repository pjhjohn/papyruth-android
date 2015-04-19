package com.montserrat.utils.etc;

import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by pjhjohn on 2015-04-19.
 */
public class Validator {
    public static boolean validateEmailAddress(String email) {
        return email.contains("@");
    }
    public static boolean validatePassword(String password) {
        return password.length() > 4;
    }
    public static boolean validateEmailAddress(EditText emailView) {
        return Validator.validateEmailAddress(emailView.getText().toString());
    }

    public static boolean validatePassword(EditText passwordView) {
        return Validator.validatePassword(passwordView.getText().toString());
    }
}
