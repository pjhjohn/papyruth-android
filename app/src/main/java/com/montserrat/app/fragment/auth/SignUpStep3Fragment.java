package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep3Fragment extends Fragment implements OnPageFocus{
    private ViewPagerController pagerController;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
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
        FloatingActionControl.getInstance().clear();
        if(this.subscription !=null && !this.subscription.isUnsubscribed()) this.subscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPageFocused() {

        FloatingActionControl.getInstance().setControl(R.layout.fab_next).show(true);
        this.subscription.add(FloatingActionControl
                .clicks()
                .subscribe(unused -> {
                    if (this.pagerController.getPreviousPage() == AppConst.ViewPager.Auth.SIGNUP_STEP4) {
                        if (this.pagerController.getHistoryCopy().contains(AppConst.ViewPager.Auth.SIGNUP_STEP3)) this.pagerController.popCurrentPage();
                        else this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP4, true);
                    } else this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP4, true);
//                    this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP4, true);
                }, error -> Timber.d("page change error %s", error))
        );
    }
}
