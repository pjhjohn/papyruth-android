package com.montserrat.utils.validator;

import com.montserrat.controller.AppConst;

import rx.android.widget.OnTextChangeEvent;
import rx.functions.Func1;

/**
 * Created by pjhjohn on 2015-05-06.
 * RxValidation provides mapper functions for rx.Observable so that we could retrieve boolean result of validation.
 */

public class RxValidator {
    public static Func1<OnTextChangeEvent, Boolean> isEmpty = text -> text.text().length() == 0;
    public static Func1<OnTextChangeEvent, Boolean> isValidEmail = text -> text.text().toString().contains("@");
    public static Func1<OnTextChangeEvent, Boolean> isValidPassword = text -> text.text().length() > 0;
    public static Func1<OnTextChangeEvent, Boolean> isValidRealname = text -> text.text().toString().getBytes().length <= AppConst.MAX_REALNAME_BYTES;
    public static Func1<OnTextChangeEvent, Boolean> isValidNickname = text -> text.text().toString().getBytes().length <= AppConst.MAX_NICKNAME_BYTES;

    // TODO : Do validation on RadioGroup & Spinner
}
