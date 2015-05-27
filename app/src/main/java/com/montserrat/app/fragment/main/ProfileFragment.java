package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.montserrat.app.R;
import com.montserrat.app.fragment.nav.NavFragment;
import com.montserrat.app.model.User;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-05-19.
 */
public class ProfileFragment extends Fragment implements OnPageFocus {
    private ViewPagerController pagerController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    @InjectView (R.id.email) protected TextView email;
    @InjectView (R.id.university) protected TextView university;
    @InjectView (R.id.realname) protected TextView realname;
    @InjectView (R.id.nickname) protected TextView nickname;
    @InjectView (R.id.gender) protected TextView gender;
    @InjectView (R.id.entrance) protected TextView entrance;
    @InjectView (R.id.fab_edit) protected FloatingActionButton fab;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);

        this.fab.setOnClickListener(unused -> {
            if(this.getActivity() instanceof NavFragment.OnCategoryClickListener)
                ((NavFragment.OnCategoryClickListener)this.getActivity()).onCategorySelected(NavFragment.CategoryType.EVALUATION);
        });
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
        this.email.setText(User.getInstance().getEmail());
        this.university.setText(User.getInstance().getUniversityName());
        this.realname.setText(User.getInstance().getRealname());
        this.nickname.setText(User.getInstance().getNickname());
        this.gender.setText(this.getResources().getString(User.getInstance().getGenderIsBoy() ? R.string.gender_male : R.string.gender_female));
        this.entrance.setText(String.valueOf(User.getInstance().getAdmissionYear()));
    }
}
