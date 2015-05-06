package com.montserrat.utils.validator;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.controller.AppManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import rx.Observable;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.android.widget.OnTextChangeEvent;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by pjhjohn on 2015-05-06.
 * RxValidation provides mapper functions for rx.Observable so that we could retrieve boolean result of validation.
 */

public class RxValidator {

    /* Text Validation. WidgetObservable.text() emits OnTextChangeEvent */
    public static Func1<OnTextChangeEvent, Boolean> isEmpty = text -> text.text().length() == 0;
    public static Func1<OnTextChangeEvent, Boolean> isValidEmail = text -> text.text().toString().contains("@");
    public static Func1<OnTextChangeEvent, Boolean> isValidPassword = text -> text.text().length() > 0;
    public static Func1<OnTextChangeEvent, Boolean> isValidRealname = text -> text.text().toString().getBytes().length <= AppConst.MAX_REALNAME_BYTES;
    public static Func1<OnTextChangeEvent, Boolean> isValidNickname = text -> text.text().toString().getBytes().length <= AppConst.MAX_NICKNAME_BYTES;

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
        } /* buttons has RadioButtons */

        return Observable.merge(
            Observable.defer(() -> Observable.just(group.getCheckedRadioButtonId())),
            Observable.from(buttons).flatMap(ViewObservable::clicks).map(event -> event.view().getId())
        );
    }
    public static Func1<Integer, Boolean> isValidRadioButton = id -> id != -1;

    // TODO : Do validation on Spinner -> Is it necessary ?
}
