package com.papyruth.android.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.activity.SplashActivity;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.R;
import com.papyruth.android.papyruth;
import com.papyruth.utils.support.error.ErrorHandler;
import com.papyruth.utils.support.fab.FloatingActionControl;
import com.papyruth.utils.support.materialdialog.TermOfServicesDialog;
import com.papyruth.utils.support.picasso.ColorFilterTransformation;
import com.papyruth.utils.support.retrofit.apis.Api;
import com.papyruth.utils.view.ToolbarUtil;
import com.papyruth.utils.view.navigator.Navigator;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SettingsFragment extends Fragment {
    private Navigator navigator;
    private Context context;
    private Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = ((papyruth) getActivity().getApplication()).getTracker();
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.navigator = (Navigator) activity;
        this.context = activity;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        this.navigator = null;
        this.context = null;
    }

    @InjectView (R.id.signout_container) protected RelativeLayout signout;
    @InjectView (R.id.signout_icon) protected ImageView signoutIcon;
    @InjectView (R.id.osl_container) protected RelativeLayout openSourceLicense;
    @InjectView (R.id.osl_icon) protected ImageView openSourceLicenseIcon;
    @InjectView (R.id.tos_container) protected RelativeLayout termOfServices;
    @InjectView (R.id.tos_icon) protected ImageView termOfServicesIcon;
    @InjectView (R.id.history_delete_container) protected RelativeLayout delHistory;
    @InjectView (R.id.history_delete_icon) protected ImageView delHistoryIcon;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;
    private MaterialDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        Picasso.with(context).load(R.drawable.ic_light_evaluation).transform(new ColorFilterTransformation(Color.GRAY)).into(termOfServicesIcon);
        Picasso.with(context).load(R.drawable.ic_light_evaluation).transform(new ColorFilterTransformation(Color.GRAY)).into(openSourceLicenseIcon);
        Picasso.with(context).load(R.drawable.logout).transform(new ColorFilterTransformation(Color.GRAY)).into(signoutIcon);
        Picasso.with(context).load(R.drawable.ic_light_history).transform(new ColorFilterTransformation(Color.GRAY)).into(delHistoryIcon);
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_main_setting));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        toolbar.setTitle(R.string.toolbar_settings);
        ToolbarUtil.getColorTransitionAnimator(toolbar, R.color.toolbar_blue).start();
        FloatingActionControl.getInstance().hide(true);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, false);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, false);

        this.subscriptions.add(ViewObservable
                .clicks(this.termOfServices)
                .filter(unused -> this.dialog == null || !this.dialog.isShowing())
                .subscribe(unused -> {
                    dialog = TermOfServicesDialog.build(
                        context,
                        context.getResources().getString(R.string.label_tos),
                        context.getResources().getString(R.string.lorem_ipsum)
                    );
                    dialog.show();
                }, error -> ErrorHandler.throwError(error, this))
        );

        this.subscriptions.add(ViewObservable
            .clicks(this.openSourceLicense)
            .subscribe(unused -> this.navigator.navigate(OpenSourceLicensesFragment.class, true)
            , error->ErrorHandler.throwError(error, this))
        );

        this.subscriptions.add(ViewObservable
                .clicks(delHistory)
                .subscribe(unused -> {
                    AppManager.getInstance().clear(AppConst.Preference.HISTORY);
                    Toast.makeText(getActivity(), R.string.success_del_history, Toast.LENGTH_LONG).show();
                }, error->ErrorHandler.throwError(error, this))
        );

        this.subscriptions.add(ViewObservable
            .clicks(signout)
            .subscribe(unused -> {
                AppManager.getInstance().clear(AppConst.Preference.HISTORY);
                AppManager.getInstance().remove(AppConst.Preference.ACCESS_TOKEN);
                Api.papyruth().users_sign_out(User.getInstance().getAccessToken())
                    .observeOn(Schedulers.io())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        User.getInstance().clear();
                        this.getActivity().startActivity(new Intent(this.getActivity(), SplashActivity.class));
                        this.getActivity().finish();
                    }, error -> ErrorHandler.throwError(error, this));
            }, error->ErrorHandler.throwError(error, this))
        );
    }
}