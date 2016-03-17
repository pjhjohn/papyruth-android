package com.papyruth.android.fragment.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.activity.SplashActivity;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.fragment.TrackerFragment;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.navigator.Navigator;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SettingsFragment extends TrackerFragment {
    private Navigator mNavigator;
    private MainActivity mActivity;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mNavigator = (Navigator) activity;
        mActivity = (MainActivity) activity;
    }

    @Bind(R.id.signout_container)       protected RelativeLayout mSignOut;
    @Bind(R.id.signout_icon)            protected ImageView mSignOutIcon;
    @Bind(R.id.osl_container)           protected RelativeLayout mOpenSourceLicense;
    @Bind(R.id.osl_icon)                protected ImageView mOpenSourceLicenseIcon;
    @Bind(R.id.tos_container)           protected RelativeLayout mTermsOfService;
    @Bind(R.id.tos_icon)                protected ImageView mTermsOfServiceIcon;
    @Bind(R.id.clear_history_container) protected RelativeLayout mClearHistory;
    @Bind(R.id.clear_history_icon)      protected ImageView mClearHistoryIcon;
    @Bind(R.id.special_thanks_to)       protected TextView mThanksTo;
    private CompositeSubscription mCompositeSubscription;
    private Toolbar mToolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        mCompositeSubscription = new CompositeSubscription();
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        Picasso.with(mActivity).load(R.drawable.ic_terms_of_service_24dp).transform(new ColorFilterTransformation(mActivity.getResources().getColor(R.color.icon_material))).into(mTermsOfServiceIcon);
        Picasso.with(mActivity).load(R.drawable.ic_open_source_license_24dp).transform(new ColorFilterTransformation(mActivity.getResources().getColor(R.color.icon_material))).into(mOpenSourceLicenseIcon);
        Picasso.with(mActivity).load(R.drawable.ic_history_24dp).transform(new ColorFilterTransformation(mActivity.getResources().getColor(R.color.icon_material))).into(mClearHistoryIcon);
        Picasso.with(mActivity).load(R.drawable.ic_signout_24dp).transform(new ColorFilterTransformation(mActivity.getResources().getColor(R.color.icon_material))).into(mSignOutIcon);
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        if(mCompositeSubscription ==null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mToolbar.setTitle(R.string.toolbar_settings);
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_blue).start();
        StatusBarHelper.changeColorTo(mActivity, R.color.status_bar_blue);
        FloatingActionControl.getInstance().hide(true);
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SEARCH, false);
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SETTING, false);
        mCompositeSubscription.clear();

        if(User.getInstance().getUniversityId() == 2) mThanksTo.setText("Special Thanks to : 신승수");

        mCompositeSubscription.add(ViewObservable.clicks(mTermsOfService).subscribe(unused -> mNavigator.navigate(TermsOfServiceFragment.class, true), error -> ErrorHandler.handle(error, this)));
        mCompositeSubscription.add(ViewObservable.clicks(mOpenSourceLicense).subscribe(unused -> mNavigator.navigate(OpenSourceLicensesFragment.class, true), error -> ErrorHandler.handle(error, this)));
        mCompositeSubscription.add(ViewObservable.clicks(mClearHistory).subscribe(
            unused -> {
                AppManager.getInstance().clear(AppConst.Preference.HISTORY);
                Toast.makeText(mActivity, R.string.toast_settings_history_clear_succeed, Toast.LENGTH_SHORT).show();
            },
            error -> {
                ErrorHandler.handle(error, this);
                Toast.makeText(mActivity, R.string.toast_settings_history_clear_failed, Toast.LENGTH_SHORT).show();
            }
        ));
        mCompositeSubscription.add(ViewObservable.clicks(mSignOut).subscribe(
            unused -> {
                AppManager.getInstance().clear(AppConst.Preference.HISTORY);
                AppManager.getInstance().remove(AppConst.Preference.ACCESS_TOKEN);
                Api.papyruth().post_users_sign_out(User.getInstance().getAccessToken())
                    .observeOn(Schedulers.io())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        User.getInstance().clear();
                        mActivity.startActivity(new Intent(mActivity, SplashActivity.class));
                        mActivity.finish();
                    }, error -> ErrorHandler.handle(error, this, true));
            }, error -> ErrorHandler.handle(error, this))
        );
    }
}
