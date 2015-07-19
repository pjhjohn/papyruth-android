package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.montserrat.app.R;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.navigator.Navigator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep3Fragment extends Fragment{
//    private ViewPagerController pagerController;

    private Navigator navigator;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        this.pagerController = (ViewPagerController) activity;
        this.navigator = (Navigator)activity;
    }

    private CompositeSubscription subscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step3, container, false);
        ButterKnife.inject(this, view);
        this.subscription = new CompositeSubscription();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscription !=null && !this.subscription.isUnsubscribed()) this.subscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();

        FloatingActionControl.getInstance().setControl(R.layout.fab_next);
        this.subscription.add(FloatingActionControl
                .clicks()
                .subscribe(unused -> {
                    this.navigator.navigate(SignUpStep3Fragment.class, true);
                })
        );
    }

}
