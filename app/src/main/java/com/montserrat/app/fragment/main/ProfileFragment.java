package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gc.materialdesign.views.ButtonFlat;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.activity.AuthActivity;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.navigator.Navigator;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.view.ViewObservable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-05-19.
 */
public class ProfileFragment extends Fragment {
    private Navigator navigator;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.navigator = (Navigator) activity;
    }

    @InjectView (R.id.email) protected MaterialEditText email;
    @InjectView (R.id.university) protected MaterialEditText university;
    @InjectView (R.id.realname) protected MaterialEditText realname;
    @InjectView (R.id.nickname) protected MaterialEditText nickname;
    @InjectView (R.id.gender) protected MaterialEditText gender;
    @InjectView (R.id.entrance) protected MaterialEditText entrance;
    @InjectView (R.id.my_evaluation) protected ButtonFlat myEvaluation;
    @InjectView (R.id.my_comment) protected ButtonFlat myComment;
    @InjectView (R.id.sign_out) protected ButtonFlat signout;
    private CompositeSubscription subscriptions;

    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.email.setEnabled(false);
        this.university.setEnabled(false);
        this.realname.setEnabled(false);
        this.nickname.setEnabled(false);
        this.gender.setEnabled(false);
        this.entrance.setEnabled(false);

        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        this.toolbar.setTitle(R.string.toolbar_profile);
        this.toolbar.setTitleTextColor(Color.WHITE);
        ToolbarUtil.getColorTransitionAnimator(toolbar, AppConst.COLOR_POINT_GPA_SATISFACTION).start();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(toolbar.getY() < 0) ToolbarUtil.show(toolbar);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        final int ANIMATION_DELAY = 200;
        if(toolbar.getY() < 0) ToolbarUtil.show(toolbar);
        FloatingActionControl.getInstance().setControl(R.layout.fam_profile).show(true, ANIMATION_DELAY, TimeUnit.MILLISECONDS);

        this.email.setText(User.getInstance().getEmail());
        this.university.setText(User.getInstance().getUniversityName());
        this.realname.setText(User.getInstance().getRealname());
        this.nickname.setText(User.getInstance().getNickname());
        this.gender.setText(this.getResources().getString(User.getInstance().getGenderIsBoy() ? R.string.gender_male : R.string.gender_female));
        this.entrance.setText(String.format("%d  %s", User.getInstance().getEntranceYear(), getResources().getString(R.string.entrance_postfix)));

        this.subscriptions.add(FloatingActionControl
            .clicks(R.id.fab_edit_profile)
            .subscribe(unused -> this.navigator.navigate(ProfileEditFragment.class, true))
        );

        this.subscriptions.add(FloatingActionControl
            .clicks(R.id.fab_edit_password)
            .subscribe(unused -> this.navigator.navigate(ProfileEditPasswordFragment.class, true))
        );

        myComment.setRippleSpeed(20);
        myEvaluation.setRippleSpeed(20);
        signout.setRippleSpeed(20);

        this.subscriptions.add(ViewObservable.clicks(this.myEvaluation).subscribe(unuse -> this.navigator.navigate(MyEvaluationFragment.class, true), error -> error.printStackTrace()));
        this.subscriptions.add(ViewObservable.clicks(this.myComment).subscribe(unuse->this.navigator.navigate(MyCommentFragment.class, true), error->error.printStackTrace()));

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
