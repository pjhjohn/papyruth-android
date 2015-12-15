package com.papyruth.support.opensource.rx;

import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.model.unique.EvaluationForm;

import rx.Observable;
import rx.android.widget.OnTextChangeEvent;
import rx.functions.Func1;
import rx.functions.Func2;
import timber.log.Timber;

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

    /* for SeekBar Validation */
    public static final int ON_STOP_TRACKING_TOUCH_SEEKBAR = -1;
    public static Func1<Integer, Boolean> isIntegerValueInRange = value -> value != null && value >= 1 && value <= 10;
    public static Func2<TextView, Integer, Integer> assignProgressValue = (text, value) -> {
        if(text == null || value == ON_STOP_TRACKING_TOUCH_SEEKBAR) return value;
        if(value < 1) {
            text.setTextSize(32);
            text.setText("N/A");
        } else {
            text.setTextSize(48);
            text.setText(value >= 10? "10" : String.format("%d", value));
        } return value;
    };
    public static Observable<Integer> createObservableSeekBar(SeekBar seekbar, Boolean fromUserOnly) {
        return Observable.create(observer ->
            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(fromUserOnly && !fromUser) return;
                    if(EvaluationForm.getInstance().isEditMode() && !EvaluationForm.getInstance().isEdited())
                        EvaluationForm.getInstance().setEdited(true);
                    observer.onNext(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    observer.onNext(seekBar.getProgress());
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    observer.onNext(ON_STOP_TRACKING_TOUCH_SEEKBAR);
                }
            })
        );
    }

    /* for RatingBar Validation */
    public static Func1<Float, Boolean> isFloatValueInRange = value -> value != null && value >= 0 && value <= 10;
    public static Func2<TextView, Float, Float> assignRatingValue = (text, value) -> {
        if(text == null) return value;
        if(value < 1.0f) {
            text.setTextSize(32);
            text.setText("N/A");
        } else {
            text.setTextSize(48);
            text.setText(value >= 5.0f? "10" : String.format("%d", (int)(2*value)));
        } return value;
    };
    public static Observable<Float> createObservableRatingBar(RatingBar ratingbar, Boolean fromUserOnly) {
        return Observable.create(observer -> ratingbar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if(fromUserOnly && !fromUser) return;
            observer.onNext(rating);
        }));
    }
}
