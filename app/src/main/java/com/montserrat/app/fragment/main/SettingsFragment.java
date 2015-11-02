package com.montserrat.app.fragment.main;

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

import com.afollestad.materialdialogs.MaterialDialog;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.activity.AuthActivity;
import com.montserrat.app.activity.MainActivity;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.materialdialog.TermOfServicesDialog;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.navigator.Navigator;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.view.ViewObservable;
import rx.subscriptions.CompositeSubscription;

public class SettingsFragment extends Fragment {
    private Navigator navigator;
    private Context context;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.navigator = (Navigator) activity;
        this.context = activity;
        ((MainActivity) activity).setMenuItemVisibility(AppConst.Menu.MENU_SETTING, false);
        ((MainActivity) activity).setMenuItemVisibility(AppConst.Menu.MENU_SEARCH, false);
    }
    @Override
    public void onDetach() {
        super.onDetach();
        this.navigator = null;
        ((MainActivity) this.getActivity()).setMenuItemVisibility(AppConst.Menu.MENU_SETTING, true);
        ((MainActivity) this.getActivity()).setMenuItemVisibility(AppConst.Menu.MENU_SEARCH, false);
    }

    @InjectView (R.id.signout_container) protected RelativeLayout signout;
    @InjectView (R.id.signout_icon) protected ImageView signoutIcon;
    @InjectView (R.id.osl_container) protected RelativeLayout openSourceLicense;
    @InjectView (R.id.osl_icon) protected ImageView openSourceLicenseIcon;
    @InjectView (R.id.tos_container) protected RelativeLayout termOfServices;
    @InjectView (R.id.tos_icon) protected ImageView termOfServicesIcon;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;
    private MaterialDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.toolbar_settings);
        ToolbarUtil.getColorTransitionAnimator(toolbar, AppConst.COLOR_POINT_GPA_SATISFACTION).start();
        Picasso.with(context).load(R.drawable.ic_light_evaluation).transform(new ColorFilterTransformation(Color.GRAY)).into(termOfServicesIcon);
        Picasso.with(context).load(R.drawable.ic_light_evaluation).transform(new ColorFilterTransformation(Color.GRAY)).into(openSourceLicenseIcon);
        Picasso.with(context).load(R.drawable.logout).transform(new ColorFilterTransformation(Color.GRAY)).into(signoutIcon);
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
        FloatingActionControl.getInstance().hide(true);

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
            })
        );

        this.subscriptions.add(ViewObservable
            .clicks(this.openSourceLicense)
            .subscribe(unused -> this.navigator.navigate(OpenSourceLicensesFragment.class, true))
        );

        this.subscriptions.add(ViewObservable
            .clicks(signout)
            .subscribe(unused -> {
                AppManager.getInstance().clear(AppConst.Preference.HISTORY);
                AppManager.getInstance().remove(AppConst.Preference.ACCESS_TOKEN);
                User.getInstance().clear();
                this.getActivity().startActivity(new Intent(this.getActivity(), AuthActivity.class));
                this.getActivity().finish();
            })
        );
    }
}
