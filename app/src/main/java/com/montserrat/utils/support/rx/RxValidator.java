package com.montserrat.utils.support.rx;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.model.unique.EvaluationForm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import rx.Observable;
import rx.android.view.ViewObservable;
import rx.android.widget.OnTextChangeEvent;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by pjhjohn on 2015-05-06.
 * RxValidation provides mapper functions for rx.Observable so that we could retrieve boolean result of validation.
 */

public class RxValidator {
    /* for Text Validation. WidgetObservable.text() emits OnTextChangeEvent */
    public static Func1<OnTextChangeEvent, String> toString = text -> text.text().toString();
    public static Func1<String, Boolean> isEmpty  = text -> text.length() == 0;
    public static Func1<String, Boolean> nonEmpty = text -> text.length() > 0;
    public static Func1<String, Boolean> isValidEmail    = text -> nonEmpty.call(text) && text.contains("@");
    public static Func1<String, Boolean> isValidPassword = text -> nonEmpty.call(text) && text.length() > 0;
    public static Func1<String, Boolean> isValidRealname = text -> nonEmpty.call(text) && text.getBytes().length <= AppConst.MAX_REALNAME_BYTES;
    public static Func1<String, Boolean> isValidNickname = text -> nonEmpty.call(text) && text.getBytes().length <= AppConst.MAX_NICKNAME_BYTES;
    public static Func1<String, Boolean> isValidEvaluationBody = text -> nonEmpty.call(text) && text.getBytes().length >= AppConst.MIN_EVALUATION_BODY_BYTES;

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

    /* for RadioGroup Validation. */
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

    /* for SeekBar Validation */
    public static Func1<Integer, Boolean> isIntegerValueInRange = value -> value != null && value >= 0 && value <= 10;
    public static Func2<TextView, Integer, Integer> assignProgressValue = (text, value) -> {
        if(text == null) return value;
        if(value >= 10) text.setText("10");
        else if(value < 0) text.setText("N/A");
        else text.setText(String.format("%d", value));
        return value;
    };
    public static Observable<Integer> createObservableSeekBar(SeekBar seekbar, Boolean fromUserOnly) {
        return Observable.create(observer ->
            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(fromUserOnly && !fromUser) return;
                    if(EvaluationForm.getInstance().isModifyMode() && !EvaluationForm.getInstance().isEdit())
                        EvaluationForm.getInstance().setEdit(true);
                    observer.onNext(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    observer.onNext(seekBar.getProgress());
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    observer.onNext(seekBar.getProgress());
                }
            })
        );
    }

    /* for RatingBar Validation */
    public static Func1<Float, Boolean> isFloatValueInRange = value -> value != null && value >= 0 && value <= 10;
    public static Func2<TextView, Float, Float> assignRatingValue = (text, value) -> {
        if(text == null) return value;
        if(value >= 5.0f) text.setText("10");
        else if(value < 0.0f) text.setText("N/A");
        else text.setText(String.format("%d", (int)(2*value)));
        return value;
    };
    public static Observable<Float> createObservableRatingBar(RatingBar ratingbar, Boolean fromUserOnly) {
        return Observable.create(observer -> ratingbar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if(fromUserOnly && !fromUser) return;
            observer.onNext(rating);
        }));
    }
}
