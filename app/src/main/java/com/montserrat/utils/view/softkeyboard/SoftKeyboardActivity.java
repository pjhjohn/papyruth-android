package com.montserrat.utils.view.softkeyboard;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;

import com.montserrat.app.R;

/**
 * Created by pjhjohn on 2015-11-05.
 */
public class SoftKeyboardActivity extends Activity {
    private ViewTreeObserver.OnGlobalLayoutListener mSoftKeyboardGolbalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int heightDiff = mActivityViewGroup.getRootView().getHeight() - mActivityViewGroup.getHeight();
            int contentViewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(SoftKeyboardActivity.this);
            if(heightDiff <= contentViewTop){
                if(onHideSoftKeyboard != null) onHideSoftKeyboard.onHideSoftKeyboard();
                Intent intent = new Intent("KeyboardWillHide");
                broadcastManager.sendBroadcast(intent);
            } else {
                int keyboardHeight = heightDiff - contentViewTop;
                if(onShowSoftKeyboard != null) onShowSoftKeyboard.onShowSoftKeyboard(keyboardHeight);
                Intent intent = new Intent("KeyboardWillShow");
                intent.putExtra("KeyboardHeight", keyboardHeight);
                broadcastManager.sendBroadcast(intent);
            }
        }
    };

    private boolean mSoftKeyboardListenerAttached = false;
    private ViewGroup mActivityViewGroup;
    private OnShowSoftKeyboard onShowSoftKeyboard;
    private OnHideSoftKeyboard onHideSoftKeyboard;

    protected void attachSoftKeyboardListeners() {
        if (mSoftKeyboardListenerAttached) return;
        mActivityViewGroup = (ViewGroup) findViewById(R.id.activity_root);
        mActivityViewGroup.getViewTreeObserver().addOnGlobalLayoutListener(mSoftKeyboardGolbalLayoutListener);
        mSoftKeyboardListenerAttached = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mSoftKeyboardListenerAttached) return;
        mActivityViewGroup.getViewTreeObserver().removeGlobalOnLayoutListener(mSoftKeyboardGolbalLayoutListener);
    }

    public final void setOnShowSoftKeyboard(OnShowSoftKeyboard onShowSoftKeyboard) {
        this.onShowSoftKeyboard = onShowSoftKeyboard;
    }

    public final void setOnHideSoftKeyboard(OnHideSoftKeyboard onHideSoftKeyboard) {
        this.onHideSoftKeyboard = onHideSoftKeyboard;
    }
}
