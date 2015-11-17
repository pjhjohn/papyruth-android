package com.papyruth.utils.view.search;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by SSS on 2015-11-07.
 */
public class PreImeEditText  extends EditText {

    public interface PreImeListener{
        public void onBackPreIme();
    }

    private PreImeListener listener;

    public PreImeEditText(Context context) {
        super(context);
    }

    public PreImeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreImeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if(listener != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            listener.onBackPreIme();
            return true;
        }
        return super.dispatchKeyEventPreIme(event);
    }
    public void setPreImeListener(PreImeListener listener){
        this.listener = listener;
    }
}