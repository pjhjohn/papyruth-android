package com.papyruth.android.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.R;
import com.papyruth.android.fragment.auth.AuthFragment;
import com.papyruth.android.PapyruthApplication;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.utility.customview.FloatingActionControlContainer;
import com.papyruth.support.utility.error.ErrorHandlerCallback;
import com.papyruth.support.utility.helper.PermissionHelper;
import com.papyruth.support.utility.navigator.FragmentNavigator;
import com.papyruth.support.utility.navigator.NavigationCallback;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.softkeyboard.SoftKeyboardActivity;
import com.papyruth.support.utility.viewpager.ViewPagerController;

import timber.log.Timber;

/**
 * Activity For Authentication.
 */
public class AuthActivity extends SoftKeyboardActivity implements Navigator, ErrorHandlerCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    private FragmentNavigator mNavigator;
    private Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = ((PapyruthApplication) getApplication()).getTracker();
        this.setContentView(R.layout.activity_auth);
        this.attachSoftKeyboardListeners();
        FloatingActionControl.getInstance().setContainer((FloatingActionControlContainer) this.findViewById(R.id.fac));
        mNavigator = new FragmentNavigator(this.getFragmentManager(), R.id.auth_navigator, AuthFragment.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        ((InputMethodManager)this.getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
        ViewHolderFactory.getInstance().setContext(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((InputMethodManager)this.getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        boolean backed = false;
        if(mViewPagerController.controlTargetContains(mViewPagerController.getCurrentPage())) return;
        if(mViewPagerController != null && mViewPagerController.back()) backed = true;
        if(!backed && mNavigator.back()) backed = true;
        if(!backed) this.finish();
    }

    /* Map Navigator methods to mNavigator */
    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack) {
        mNavigator.navigate(target, addToBackStack);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, Navigator.AnimatorType animatorType) {
        mNavigator.navigate(target, addToBackStack, animatorType);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, boolean clear) {
        mNavigator.navigate(target, addToBackStack, clear);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, Navigator.AnimatorType animatorType, boolean clear) {
        mNavigator.navigate(target, addToBackStack, animatorType, clear);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack) {
        mNavigator.navigate(target, bundle, addToBackStack);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, Navigator.AnimatorType animatorType) {
        mNavigator.navigate(target, bundle, addToBackStack, animatorType);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, boolean clear) {
        mNavigator.navigate(target, bundle, addToBackStack, clear);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, Navigator.AnimatorType animatorType, boolean clear) {
        mNavigator.navigate(target, bundle, addToBackStack, animatorType, clear);
    }

    @Override
    public String getBackStackNameAt(int index) {
        return mNavigator.getBackStackNameAt(index);
    }

    @Override
    public boolean back() {
        return mNavigator.back();
    }

    @Override
    public void setOnNavigateListener(NavigationCallback listener) {
        mNavigator.setOnNavigateListener(listener);
    }

    /* ViewPagerController Mapper */
    private ViewPagerController mViewPagerController;
    public ViewPagerController getViewPagerController() {
        return mViewPagerController;
    }

    public void setViewPagerController(ViewPagerController viewPagerController) {
        mViewPagerController = viewPagerController;
    }

    /* Fade-Animated Activity Transition to MainActivity */
    public void startMainActivity() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        AuthActivity.this.startActivity(intent);
        AuthActivity.this.finish();
    }

    @Override
    public void sendErrorTracker(String cause, String from, boolean isFatal) {
        Timber.d("cause : %s, from : %s", cause, from);
        mTracker.send(new HitBuilders.ExceptionBuilder()
            .setDescription(cause)
            .setFatal(isFatal)
            .build()
        );
    }

    /* Runtime Permission */
    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PermissionHelper.PERMISSION_CONTACTS:
                if (PermissionHelper.verifyPermissions(grantResults)) {
                    // TODO : Something with permission
                } else {
                    String message = PermissionHelper.getRationalMessage(this, PermissionHelper.PERMISSION_CONTACTS);
                    PermissionHelper.showRationalDialog(this, message);
                } break;
            case PermissionHelper.PERMISSION_READ_CONTACTS :
                if (PermissionHelper.verifyPermissions(grantResults)) {
                    // TODO : Something with permission
                } else {
                    String message = PermissionHelper.getRationalMessage(this, PermissionHelper.PERMISSION_READ_CONTACTS);
                    PermissionHelper.showRationalDialog(this, message);
                } break;
            default : break;
        }
    }
}