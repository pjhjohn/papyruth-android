package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.Page;
import com.montserrat.utils.view.viewpager.ViewPagerContainerController;

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

    @InjectView (R.id.email) protected TextView email;
    @InjectView (R.id.university) protected TextView university;
    @InjectView (R.id.realname) protected TextView realname;
    @InjectView (R.id.nickname) protected TextView nickname;
    @InjectView (R.id.gender) protected TextView gender;
    @InjectView (R.id.entrance) protected TextView entrance;
    private CompositeSubscription subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
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
        FloatingActionControl.getInstance().setControl(R.layout.fab_edit).show(true, 200, TimeUnit.MILLISECONDS);

        this.email.setText(User.getInstance().getEmail());
        this.university.setText(User.getInstance().getUniversityName());
        this.realname.setText(User.getInstance().getRealname());
        this.nickname.setText(User.getInstance().getNickname());
        this.gender.setText(this.getResources().getString(User.getInstance().getGenderIsBoy() ? R.string.gender_male : R.string.gender_female));
        this.entrance.setText(String.valueOf(User.getInstance().getAdmissionYear()));

        this.subscriptions.add(FloatingActionControl
            .clicks()
            .subscribe(unused -> controller.setCurrentPage(Page.at(AppConst.ViewPager.Type.PROFILE, AppConst.ViewPager.Profile.PROFILE_EDIT), true))
        );
    }
}
