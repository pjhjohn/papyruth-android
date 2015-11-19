package com.papyruth.android.fragment.auth;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.papyruth.android.AppConst;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.R;
import com.papyruth.utils.view.viewpager.FlexibleViewPager;
import com.papyruth.utils.view.viewpager.ViewPagerController;
import com.papyruth.utils.view.viewpager.ViewPagerManager;

import java.util.Stack;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AuthFragment extends Fragment implements ViewPagerController {
    @InjectView(R.id.app_logo)              protected ImageView mApplicationLogo;
    @InjectView(R.id.app_logo_horizontal)   protected ImageView mApplicationLogoHorizontal;
    @InjectView(R.id.auth_viewpager)        protected FlexibleViewPager mAuthViewPager;
    @InjectView(R.id.signup_progress)       protected ProgressBar mSignUpProgress;
    @InjectView(R.id.signup_label)          protected TextView mSignUpLabel;
    private ViewPagerManager mViewPagerManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth, container, false);
        ButterKnife.inject(this, view);
        mViewPagerManager = new ViewPagerManager(
            mAuthViewPager,
            getFragmentManager(),
            AuthFragmentFactory.getInstance(),
            AppConst.ViewPager.Auth.LENGTH
        );
        mSignUpProgress.setMax(AppConst.ViewPager.Auth.LENGTH - 1);
        mAuthViewPager.setPagerController(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    public void animateApplicationLogo(boolean animateToSignUpStep) {
        if(mViewPagerManager.getCurrentPage() != AppConst.ViewPager.Auth.SIGNUP_STEP1) return;

        mSignUpProgress.setVisibility(animateToSignUpStep ? View.GONE : View.VISIBLE);
        mApplicationLogoHorizontal.setVisibility(View.GONE);

        ViewGroup.LayoutParams lpApplicationLogo = mApplicationLogo.getLayoutParams();
        ViewGroup.LayoutParams lpSignUpLabel = mSignUpLabel.getLayoutParams();
        final int appLogoHeight = lpApplicationLogo.height;
        final int appLogoWidth  = lpApplicationLogo.width;
        final float labelTargetHeight = getResources().getDimension(R.dimen.baseline_x4);

        ValueAnimator animApplicationLogo = animateToSignUpStep? ValueAnimator.ofFloat(1, 2) : ValueAnimator.ofFloat(1, 0.5f);
        animApplicationLogo.addUpdateListener(
            animation -> {
                lpApplicationLogo.height = (int) (appLogoHeight * (float) animation.getAnimatedValue());
                lpApplicationLogo.width  = (int) (appLogoWidth * (float) animation.getAnimatedValue());
                mApplicationLogo.setLayoutParams(lpApplicationLogo);
            });

        ValueAnimator animSignUpLabel = animateToSignUpStep? ValueAnimator.ofFloat(1, 0) : ValueAnimator.ofFloat(0, 1);
        animSignUpLabel.addUpdateListener(
            animation -> {
                lpSignUpLabel.height = (int) (labelTargetHeight * (float) animation.getAnimatedValue());
                mSignUpLabel.setLayoutParams(lpSignUpLabel);
                mSignUpLabel.setAlpha((float) animation.getAnimatedValue());
            }
        );

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animApplicationLogo, animSignUpLabel);
        animSet.start();
    }

    /* Bind this to Activity as ViewPagerController */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((AuthActivity) activity).setViewPagerController(this);
    }
    @Override
    public void onDetach() {
        super.onDetach();
        ((AuthActivity) getActivity()).setViewPagerController(null);
    }

    /* ViewPagerController */
    @Override
    public Stack<Integer> getHistoryCopy() {
        return mViewPagerManager.getHistoryCopy();
    }

    @Override
    public int getPreviousPage() {
        return mViewPagerManager.getPreviousPage();
    }

    @Override
    public void setCurrentPage (int pageNum, boolean addToBackStack) {
        mSignUpProgress.setProgress(pageNum);
        mViewPagerManager.setCurrentPage(pageNum, addToBackStack);
        animateApplicationLogo(false);
    }

    @Override
    public boolean popCurrentPage () {
        return mViewPagerManager.popCurrentPage();
    }

    @Override
    public boolean back() {
        if(mSignUpProgress.getProgress() > 0) mSignUpProgress.setProgress(mSignUpProgress.getProgress() - 1);
        if (mViewPagerManager.controlTargetContains(mViewPagerManager.getCurrentPage())) {
            popCurrentPage();
            return true;
        }
        animateApplicationLogo(true);
        return mViewPagerManager.back();
    }

    @Override
    public void addImeControlFragment(int page) {
        mViewPagerManager.addImeControlFragment(page);
    }

    @Override
    public boolean controlTargetContains(int number) {
        return mViewPagerManager.controlTargetContains(number);
    }

    @Override
    public int getCurrentPage() {
        return mViewPagerManager.getCurrentPage();
    }
}