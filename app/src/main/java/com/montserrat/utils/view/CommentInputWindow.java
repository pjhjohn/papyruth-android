package com.montserrat.utils.view;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.montserrat.app.R;
import com.montserrat.utils.view.viewpager.OnBack;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-23.
 */
public class CommentInputWindow extends RelativeLayout {
    @InjectView(R.id.toolbar_comment) Toolbar toolbar;
    @InjectView(R.id.comment_input) EditText commentInput;

    public CommentInputWindow(Context context) {
        super(context);
        this.init();
    }

    public CommentInputWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public CommentInputWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.comment, this, true);
        ButterKnife.inject(this);
        this.toolbar.setTitle(R.string.toolbar_title_new_comment);
    }

    public EditText getCommentInputEditText() {
        return this.commentInput;
    }

    OnBack listener;
    public void setOnBackListener(OnBack listener) {
        this.listener = listener;
        this.toolbar.setNavigationIcon(R.drawable.ic_light_back);
        this.toolbar.setNavigationOnClickListener(view -> listener.onBack());
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && listener != null) {
            listener.onBack();
            return true;
        }
        return super.dispatchKeyEventPreIme(event);
    }
}
