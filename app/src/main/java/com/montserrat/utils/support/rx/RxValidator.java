package com.montserrat.utils.support.rx;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.montserrat.app.R;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import rx.Observable;
import rx.android.view.ViewObservable;
import rx.android.widget.OnTextChangeEvent;
import rx.functions.Func1;

/**
 * Created by pjhjohn on 2015-05-06.
 * RxValidation provides mapper functions for rx.Observable so that we could retrieve boolean result of validation.
 */

public class RxValidator {

    /* Text Validation. WidgetObservable.text() emits OnTextChangeEvent */
    public static Func1<OnTextChangeEvent, String> toString = text -> text.text().toString();
    public static Func1<String, Boolean> isEmpty  = text -> text.length() == 0;
    public static Func1<String, Boolean> nonEmpty = text -> text.length() > 0;
    public static Func1<String, Boolean> isValidEmail    = text -> nonEmpty.call(text) && text.contains("@");
    public static Func1<String, Boolean> isValidPassword = text -> nonEmpty.call(text) && text.length() > 0;
    public static Func1<String, Boolean> isValidRealname = text -> nonEmpty.call(text) && text.getBytes().length <= AppConst.MAX_REALNAME_BYTES;
    public static Func1<String, Boolean> isValidNickname = text -> nonEmpty.call(text) && text.getBytes().length <= AppConst.MAX_NICKNAME_BYTES;

    public static Func1<String, String> getErrorMessageEmail = text -> {
        if (isValidEmail.call(text)) return null;
        else if (isEmpty.call(text)) return AppManager.getInstance().getString(R.string.field_invalid_required);
        else return AppManager.getInstance().getString(R.string.field_invalid_email);
    };
    public static Func1<String, String> getErrorMessagePassword = text -> {
        if (isValidPassword.call(text)) return null;
        else if (isEmpty.call(text)) return AppManager.getInstance().getString(R.string.field_invalid_required);
        else return AppManager.getInstance().getString(R.string.field_invalid_password);
    };
    public static Func1<String, String> getErrorMessageRealname = text -> {
        if (isValidRealname.call(text)) return null;
        else if (isEmpty.call(text)) return AppManager.getInstance().getString(R.string.field_invalid_required);
        else return AppManager.getInstance().getString(R.string.field_invalid_realname);
    };
    public static Func1<String, String> getErrorMessageNickname = text -> {
        if (isValidNickname.call(text)) return null;
        else if (isEmpty.call(text)) return AppManager.getInstance().getString(R.string.field_invalid_required);
        else return AppManager.getInstance().getString(R.string.field_invalid_nickname);
    };

    /* RadioGroup Validation. */
    public static Observable<Integer> createObservableRadioGroup(RadioGroup group) {
        List<RadioButton> buttons = new ArrayList<>();
        Queue<ViewGroup> queue = new LinkedList<>();
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

        return Observable.from(buttons).flatMap(ViewObservable::clicks).map(event -> event.view().getId()).startWith(group.getCheckedRadioButtonId());
    }
    public static Func1<Integer, Boolean> isValidRadioButton = id -> id != -1;

    // TODO : Do validation on Spinner -> Is it necessary ?
}
