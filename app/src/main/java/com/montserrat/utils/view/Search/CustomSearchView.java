package com.montserrat.utils.view.search;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import com.montserrat.app.R;
import com.montserrat.utils.view.viewpager.OnBack;

/**
 * Created by SSS on 2015-07-05.
 */
public class CustomSearchView extends SearchView {
    private OnBack onBackListener;
    public CustomSearchView(Context context) {
        super(context);
    }

    public CustomSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTextStrokeColor(int color){
        EditText editText = (EditText)this.findViewById(R.id.search_src_text);
        ShapeDrawable sd = new ShapeDrawable();
        sd.getPaint().setStyle(Paint.Style.STROKE);
        sd.getPaint().setColor(color);
        sd.getPaint().setStrokeWidth(3);

        editText.setBackgroundDrawable(sd);
    }

    public void setTextFillColor(int color){
        EditText editText = (EditText)this.findViewById(R.id.search_src_text);
        ShapeDrawable sd = new ShapeDrawable();
        sd.getPaint().setStyle(Paint.Style.FILL);
        sd.getPaint().setColor(color);
        editText.setBackgroundDrawable(sd);
    }


    public void setOnBackListener(OnBack onBack){
        this.onBackListener = onBack;
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && onBackListener != null) {
            this.onBackListener.onBack();
            return true;
        }
        return super.dispatchKeyEventPreIme(event);
    }
}
