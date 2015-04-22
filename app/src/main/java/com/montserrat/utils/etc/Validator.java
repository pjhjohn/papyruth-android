package com.montserrat.utils.etc;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

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
    public static final boolean NOT_REQUIRED = false;
    private static final boolean FAIL = false;
    private static final boolean SUCCESS = true;
    public static enum Type {
        EMAIL, PASSWORD, NAME, NICKNAME, ADMISSION_YEAR
    }
    public static boolean validate(RadioGroup group) {
        return Validator.validate(group, Validator.NOT_REQUIRED);
    }

    public static boolean validate(RadioGroup group, boolean required) {
        List<RadioButton> buttons = new ArrayList<RadioButton>();
        /* Get RadioButton List of RadioGroup by running DFS */
        Queue<ViewGroup> queue = new LinkedList<ViewGroup>();
        queue.add(group);
        while (!queue.isEmpty()) {
            ViewGroup head = queue.remove();
            for (int i = 0; i < head.getChildCount(); i++) {
                View child = head.getChildAt(i);
                if (child instanceof ViewGroup) {
                    queue.add((ViewGroup) child);
                } else if (child instanceof RadioButton) {
                    buttons.add((RadioButton) child);
                } else continue;
            }
        }
        if (required && group.getCheckedRadioButtonId() == -1) {
            for (RadioButton button : buttons) button.setError("Should choose one of these");
            return Validator.FAIL;
        }
        for (RadioButton button : buttons) button.setError(null);
        return Validator.SUCCESS;
    }

    public static boolean validate(EditText field, Type textType) {
        return Validator.validate(field, textType, Validator.NOT_REQUIRED);
    }
    public static boolean validate(EditText field, Type textType, boolean required) {
        String fieldValue = field.getText().toString();
        if (required) {
            if (TextUtils.isEmpty(fieldValue)) {
                field.setError("This field is required");
                return FAIL;
            }
        }
        CharSequence validationResponse = null;
        switch(textType) {
            case EMAIL         : field.setError(validationResponse = Validator.validateEmail(fieldValue));         break;
            case PASSWORD      : field.setError(validationResponse = Validator.validatePassword(fieldValue));      break;
            case NAME          : field.setError(validationResponse = Validator.validateName(fieldValue));          break;
            case NICKNAME      : field.setError(validationResponse = Validator.validateNickName(fieldValue));      break;
            default: return Validator.FAIL;
        }
        return validationResponse == null ? Validator.SUCCESS : Validator.FAIL;
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
