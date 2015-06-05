package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.Page;
import com.montserrat.utils.view.viewpager.ViewPagerContainerController;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-05-19.
 */
public class ProfileFragment extends Fragment implements OnPageFocus {
    private ViewPagerContainerController controller;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.controller = (ViewPagerContainerController) activity;
    }

    @InjectView (R.id.email) protected MaterialEditText email;
    @InjectView (R.id.university) protected MaterialEditText university;
    @InjectView (R.id.realname) protected MaterialEditText realname;
    @InjectView (R.id.nickname) protected MaterialEditText nickname;
    @InjectView (R.id.gender) protected MaterialEditText gender;
    @InjectView (R.id.entrance) protected MaterialEditText entrance;
    private CompositeSubscription subscriptions;

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
        if(this.getUserVisibleHint()) onPageFocused();
    }

    @Override
    public void onPageFocused () {
        final int ANIMATION_DELAY = 200;
        FloatingActionControl.getInstance().setControl(R.layout.fam_profile).show(true, ANIMATION_DELAY, TimeUnit.MILLISECONDS);

        this.email.setText(User.getInstance().getEmail());
        this.university.setText(User.getInstance().getUniversityName());
        this.realname.setText(User.getInstance().getRealname());
        this.nickname.setText(User.getInstance().getNickname());
        this.gender.setText(this.getResources().getString(User.getInstance().getGenderIsBoy() ? R.string.gender_male : R.string.gender_female));
        this.entrance.setText(String.format("%d  %s", User.getInstance().getEntranceYear(), getResources().getString(R.string.entrance_postfix)));

        this.subscriptions.add(FloatingActionControl
            .clicks(R.id.fab_edit_profile)
            .subscribe(unused -> controller.setCurrentPage(Page.at(AppConst.ViewPager.Type.PROFILE, AppConst.ViewPager.Profile.PROFILE_EDIT), true))
        );

        this.subscriptions.add(FloatingActionControl
            .clicks(R.id.fab_edit_password)
            .subscribe(unused -> controller.setCurrentPage(Page.at(AppConst.ViewPager.Type.PROFILE, AppConst.ViewPager.Profile.PROFILE_EDIT_PASSWORD), true))
        );
    }
}
