package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
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
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.navigator.Navigator;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.view.ViewObservable;
import rx.subscriptions.CompositeSubscription;

public class MyInfoFragment extends Fragment {
    private Navigator navigator;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.navigator = (Navigator) activity;
    }

    @InjectView (R.id.my_info_exit) protected RelativeLayout exitLayout;
    @InjectView (R.id.my_info_exit_icon) protected ImageView exitIcon;
    @InjectView (R.id.my_info_license) protected RelativeLayout licenseLayout;
    @InjectView (R.id.my_info_license_icon) protected ImageView licenseIcon;
    @InjectView (R.id.my_info_term) protected RelativeLayout termLayout;
    @InjectView (R.id.my_info_term_icon) protected ImageView termicon;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_info, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.toolbar_profile);
        ToolbarUtil.getColorTransitionAnimator(toolbar, AppConst.COLOR_POINT_GPA_SATISFACTION).start();
        Picasso.with(getActivity()).load(R.drawable.logout).transform(new ColorFilterTransformation(this.getResources().getColor(R.color.primary_dark_material_dark))).into(exitIcon);
        Picasso.with(getActivity()).load(R.drawable.ic_light_evaluation).transform(new ColorFilterTransformation(this.getResources().getColor(R.color.primary_dark_material_dark))).into(licenseIcon);
        Picasso.with(getActivity()).load(R.drawable.ic_light_evaluation).transform(new ColorFilterTransformation(this.getResources().getColor(R.color.primary_dark_material_dark))).into(termicon);

//        this.subscriptions.add(ViewObservable.clicks(this.evaluationLayout).subscribe(unuse -> this.navigator.navigate(MyEvaluationFragment.class, true), error -> error.printStackTrace()));
//        this.subscriptions.add(ViewObservable.clicks(this.commentLayout).subscribe(unuse -> this.navigator.navigate(MyCommentFragment.class, true), error -> error.printStackTrace()));
//        this.subscriptions.add(ViewObservable.clicks(this.profileLayout).subscribe(unuse -> this.navigator.navigate(ProfileFragment.class, true), error -> error.printStackTrace()));

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
        ((MainActivity)this.getActivity()).setMenuItemVisibility(AppConst.Menu.MENU_SEARCH, true);

    }

    @Override
    public void onResume() {
        super.onResume();
        FloatingActionControl.getInstance().setControl(R.layout.fab_done);
        ((MainActivity)this.getActivity()).setMenuItemVisibility(AppConst.Menu.MENU_SEARCH, false);
        this.subscriptions.add(ViewObservable
                .clicks(exitLayout)
                .subscribe(unused -> {
                    AppManager.getInstance().clear(AppConst.Preference.HISTORY);
                    AppManager.getInstance().remove(AppConst.Preference.ACCESS_TOKEN);
                    User.getInstance().clear();
                    this.getActivity().startActivity(new Intent(this.getActivity(), AuthActivity.class));
                    this.getActivity().finish();

                })
        );

        buildTermDialog();
        buildLicenseDialog();
        this.subscriptions.add(
            ViewObservable
                .clicks(termLayout)
                .filter(unused -> !this.termPage.isShowing())
                .subscribe(unused -> {
                    termPage.show();
                },error ->{
                    error.printStackTrace();
                })
        );
        this.subscriptions.add(
            ViewObservable
                .clicks(licenseLayout)
                .filter(unused -> !this.termPage.isShowing())
                .subscribe(unused -> {
                    termPage.show();
                },error ->{
                    error.printStackTrace();
                })
        );
    }

    private MaterialDialog termPage;

    private CharSequence termContents;
    private void buildTermDialog(){
        this.termContents = this.getResources().getString(R.string.lorem_ipsum);
        this.termPage = new MaterialDialog.Builder(this.getActivity())
            .title(R.string.term)
            .content(this.termContents)
            .positiveText(R.string.confirm_positive)
            .build();
    }

    private void buildLicenseDialog(){
        this.termContents = this.getResources().getString(R.string.lorem_ipsum);
        this.termPage = new MaterialDialog.Builder(this.getActivity())
            .title(R.string.opensourse_license)
            .content(this.termContents)
            .positiveText(R.string.confirm_positive)
            .build();
    }
}
