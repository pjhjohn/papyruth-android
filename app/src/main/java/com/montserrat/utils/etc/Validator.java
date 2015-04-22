package com.montserrat.utils.etc;

import android.text.TextUtils;
import android.widget.EditText;

/**
 * Created by pjhjohn on 2015-04-19.
 */
public class Validator {
    /**
     * Validate EditText of email view and set Error Message if exist.
     * @param emailView
     * @return success flag of validation
     */
    public static final boolean REQUIRED = true;
    private static final boolean FAIL = false;
    private static final boolean SUCCESS = true;
    public static enum Type {
        EMAIL, PASSWORD, NAME, NICKNAME, GENDER, ADMISSION_YEAR
    }
    public static boolean validate(EditText field, Type fieldType) {
        return Validator.validate(field, fieldType, false);
    }
    public static boolean validate(EditText field, Type fieldType, boolean required) {
        String fieldValue = field.getText().toString();
        if (required) {
            if (TextUtils.isEmpty(fieldValue)) {
                field.setError("This field is required");
                return FAIL;
            }
        }
        CharSequence validationResponse = null;
        switch(fieldType) {
            case EMAIL         : field.setError(validationResponse = Validator.validateEmail(fieldValue));         break;
            case PASSWORD      : field.setError(validationResponse = Validator.validatePassword(fieldValue));      break;
            case NAME          : field.setError(validationResponse = Validator.validateName(fieldValue));          break;
            case NICKNAME      : field.setError(validationResponse = Validator.validateNickName(fieldValue));      break;
            default: return false;
        }
        return validationResponse == null;
    }

    public static CharSequence validateEmail(String email) {
        return email.contains("@") ? null : "Invalid Email Format."; // TODO : return error message value should be in R.string.~
    }
    public static CharSequence validatePassword(String password) {
        return password.length() > 4 ? null  : "Password is too short";
    }
    public static CharSequence validateName(String name) {
        return null;
    }
    public static CharSequence validateNickName(String nickname) {
        return null;
    }
    public static CharSequence validateGender(String gender) {
        return null;
    }
    public static CharSequence validateAdmissionYear(String admissionYear) {
        return null;
    }
}
