package com.montserrat.utils.validator;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.controller.AppManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by pjhjohn on 2015-04-19.
 * Validation class for multiple views that takes some behavior for fill-up forms.
 */
public class Validator {
    public static final boolean REQUIRED = true;
    public static final boolean NOT_REQUIRED = false;

    /* EditText */
    public static enum TextType { EMAIL, PASSWORD, NAME, NICKNAME }
    public static View validate(EditText field, TextType textType) {
        return Validator.validate(field, textType, Validator.NOT_REQUIRED);
    }
    public static View validate(EditText field, TextType textType, boolean required) {
        String fieldValue = field.getText().toString();
        if (required && TextUtils.isEmpty(fieldValue)) {
            field.setError(AppManager.getInstance().getString(R.string.field_invalid_required));
            return field;
        }
        CharSequence errorMsg = null;
        switch(textType) {
            case EMAIL         : field.setError(errorMsg = Validator.validateEmail(fieldValue));         break;
            case PASSWORD      : field.setError(errorMsg = Validator.validatePassword(fieldValue));      break;
            case NAME          : field.setError(errorMsg = Validator.validateName(fieldValue));          break;
            case NICKNAME      : field.setError(errorMsg = Validator.validateNickName(fieldValue));      break;
        }
        return errorMsg == null ? null : field;
    }

    private static CharSequence validateEmail(String email) {
        boolean success = email.contains("@");
        return success ? null : AppManager.getInstance().getString(R.string.field_invalid_email);
    }
    private static CharSequence validatePassword(String password) {
        boolean success = password.length() > 4;
        return success ? null : AppManager.getInstance().getString(R.string.field_invalid_password);
    }
    private static CharSequence validateName(String name) {
        boolean success = name.getBytes().length <= AppConst.MAX_NAME_BYTES;
        return success ? null : AppManager.getInstance().getString(R.string.field_invalid_name);
    }
    private static CharSequence validateNickName(String nickname) {
        boolean success = nickname.getBytes().length <= AppConst.MAX_NICKNAME_BYTES;
        return success ? null : AppManager.getInstance().getString(R.string.field_invalid_nickname);
    }

    /* RadioGroup - TODO : By enabling setFocusableInTouchMode, needs 2 click to actually click the button. Will find way to override it. */
    public static View validate(RadioGroup group) {
        return Validator.validate(group, Validator.NOT_REQUIRED);
    }
    public static View validate(RadioGroup group, boolean required) {
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
                }
            }
        }
        if (required && group.getCheckedRadioButtonId() == -1) {
            if (buttons.isEmpty()) return group;
            else {
                for (RadioButton button : buttons) button.setError(AppManager.getInstance().getString(R.string.field_invalid_required));
                RadioButton button = buttons.get(0);
                button.setFocusable(true);
                button.setFocusableInTouchMode(true);
                return button;
            }
        }
        if(buttons.isEmpty()) return group;
        else {
            for (RadioButton button : buttons) button.setError(null);
            return null;
        }
    }

    /* Spinner - TODO : ONLY (!) mark is showed. not critical. */
    public static enum SpinnerType {
        ADMISSION
    }
    public static View validate(Spinner spinner, SpinnerType spinnerType) {
        return Validator.validate(spinner, spinnerType, Validator.NOT_REQUIRED);
    }
    public static View validate(Spinner spinner, SpinnerType spinnerType, boolean required) {
        Object activeSpinnerItem = spinner.getSelectedItem();
        if(required && activeSpinnerItem == null) return null;

        CharSequence errorMsg = null;
        View errorView = null;
        switch(spinnerType) {
            case ADMISSION:
                errorMsg = Validator.validateAdmissionYear(activeSpinnerItem);
                errorView = spinner.getSelectedView();
                if(errorView instanceof TextView) ((TextView) errorView).setError(errorMsg);
                break;
        }

        if(errorMsg == null) return null;
        else if(errorView == null) return null;
        else {
            errorView.setFocusable(true);
            errorView.setFocusableInTouchMode(true);
            return errorView;
        }
    }
    private static CharSequence validateAdmissionYear(Object admissionYear) {
        if (admissionYear == null) return AppManager.getInstance().getString(R.string.field_invalid_admission_year);
        else return (Integer) admissionYear >= AppConst.MIN_ADMISSION_YEAR ? null : AppManager.getInstance().getString(R.string.field_invalid_admission_year);
    }
}
