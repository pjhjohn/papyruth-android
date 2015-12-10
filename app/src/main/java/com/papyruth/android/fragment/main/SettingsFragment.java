package com.papyruth.android.fragment.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.activity.SplashActivity;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.materialdialog.TermOfServicesDialog;
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
    private Navigator navigator;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Bind(R.id.signout_container) protected RelativeLayout signout;
    @Bind(R.id.signout_icon) protected ImageView signoutIcon;
    @Bind(R.id.osl_container) protected RelativeLayout openSourceLicense;
    @Bind(R.id.osl_icon) protected ImageView openSourceLicenseIcon;
    @Bind(R.id.tos_container) protected RelativeLayout termOfServices;
    @Bind(R.id.tos_icon) protected ImageView termOfServicesIcon;
    @Bind(R.id.history_delete_container) protected RelativeLayout delHistory;
    @Bind(R.id.history_delete_icon) protected ImageView delHistoryIcon;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;
    private MaterialDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        this.subscriptions = new CompositeSubscription();
        toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        Picasso.with(context).load(R.drawable.ic_terms_of_service_24dp).transform(new ColorFilterTransformation(context.getResources().getColor(R.color.icon_material))).into(termOfServicesIcon);
        Picasso.with(context).load(R.drawable.ic_open_source_license_24dp).transform(new ColorFilterTransformation(context.getResources().getColor(R.color.icon_material))).into(openSourceLicenseIcon);
        Picasso.with(context).load(R.drawable.ic_history_24dp).transform(new ColorFilterTransformation(context.getResources().getColor(R.color.icon_material))).into(delHistoryIcon);
        Picasso.with(context).load(R.drawable.ic_signout_24dp).transform(new ColorFilterTransformation(context.getResources().getColor(R.color.icon_material))).into(signoutIcon);
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        toolbar.setTitle(R.string.toolbar_settings);
        ToolbarHelper.getColorTransitionAnimator(toolbar, R.color.toolbar_blue).start();
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_blue);
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
            }, error -> ErrorHandler.handle(error, this))
        );

        this.subscriptions.add(ViewObservable
            .clicks(this.openSourceLicense)
            .subscribe(unused -> this.navigator.navigate(OpenSourceLicensesFragment.class, true)
            , error->ErrorHandler.handle(error, this))
        );

        this.subscriptions.add(ViewObservable
                .clicks(delHistory)
                .subscribe(unused -> {
                    AppManager.getInstance().clear(AppConst.Preference.HISTORY);
                    Toast.makeText(getActivity(), R.string.success_del_history, Toast.LENGTH_LONG).show();
                }, error->ErrorHandler.handle(error, this))
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
                    }, error -> ErrorHandler.handle(error, this));
            }, error->ErrorHandler.handle(error, this))
        );
    }
}
