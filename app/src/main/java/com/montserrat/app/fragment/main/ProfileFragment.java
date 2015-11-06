package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.activity.MainActivity;
import com.montserrat.app.model.unique.Statistics;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.navigator.Navigator;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
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
    @Override
    public void onDetach() {
        super.onDetach();
        this.navigator = null;
        ((MainActivity)this.getActivity()).setMenuItemVisibility(AppConst.Menu.MENU_SETTING, false);
        ((MainActivity)this.getActivity()).setMenuItemVisibility(AppConst.Menu.MENU_SEARCH, true);
    }

    @InjectView (R.id.university_image) protected ImageView universityImage;
    @InjectView (R.id.university_name) protected TextView universityName;
    @InjectView (R.id.entrance) protected TextView entrance;
    @InjectView (R.id.email_icon) protected ImageView emailIcon;
    @InjectView (R.id.email_text) protected TextView email;
    @InjectView (R.id.realname_icon) protected ImageView realnameIcon;
    @InjectView (R.id.realname_text) protected TextView realname;
    @InjectView (R.id.nickname_icon) protected ImageView nicknameIcon;
    @InjectView (R.id.nickname_text) protected TextView nickname;
    @InjectView (R.id.gender_icon) protected ImageView genderIcon;
    @InjectView (R.id.gender_text) protected TextView gender;

    private CompositeSubscription subscriptions;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
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
        final Context context = this.getActivity();
        this.toolbar.setTitle(R.string.toolbar_profile);
        ToolbarUtil.getColorTransitionAnimator(toolbar, AppConst.COLOR_POINT_GPA_SATISFACTION).start();
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.MENU_SETTING, true);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.MENU_SEARCH, false);

        FloatingActionControl.getInstance().setControl(R.layout.fam_profile).show(true, AppConst.ANIM_DURATION_SHORT, TimeUnit.MILLISECONDS);

        Picasso.with(context).load(User.getInstance().getUniversityImageUrl()).into(this.universityImage);
        this.universityName.setText(User.getInstance().getUniversityName());
        this.entrance.setText(String.format("%d  %s", User.getInstance().getEntranceYear(), getResources().getString(R.string.entrance_postfix)));
        Picasso.with(context).load(R.drawable.ic_light_email).transform(new ColorFilterTransformation(Color.GRAY)).into(this.emailIcon);
        this.email.setText(User.getInstance().getEmail());
        Picasso.with(context).load(R.drawable.ic_light_person).transform(new ColorFilterTransformation(Color.GRAY)).into(this.realnameIcon);
        this.realname.setText(User.getInstance().getRealname());
        Picasso.with(context).load(R.drawable.ic_light_person).transform(new ColorFilterTransformation(Color.GRAY)).into(this.nicknameIcon);
        this.nickname.setText(User.getInstance().getNickname());
        Picasso.with(context).load(R.drawable.ic_light_gender).transform(new ColorFilterTransformation(Color.GRAY)).into(this.genderIcon);
        this.gender.setText(this.getResources().getString(User.getInstance().getGenderIsBoy() ? R.string.gender_male : R.string.gender_female));

        this.subscriptions.add(FloatingActionControl
            .clicks(R.id.fab_edit_email)
            .subscribe(unused -> this.navigator.navigate(ProfileChangeEmailFragment.class, true))
        );
        this.subscriptions.add(FloatingActionControl
            .clicks(R.id.fab_edit_nickname)
            .subscribe(unused -> this.navigator.navigate(ProfileChangeNicknameFragment.class, true))
        );
        this.subscriptions.add(FloatingActionControl
            .clicks(R.id.fab_edit_password)
            .subscribe(unused -> this.navigator.navigate(ProfileChangePasswordFragment.class, true))
        );
    }
}
