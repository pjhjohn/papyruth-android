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
    private ViewPagerManager manager;

    @InjectView(R.id.application_logo) protected ImageView applicationLogo;
    @InjectView(R.id.signup_progress) protected ProgressBar progress;
    @InjectView(R.id.auth_viewpager) protected FlexibleViewPager viewPager;
    @InjectView(R.id.state_name) protected TextView stateName;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth, container, false);
        ButterKnife.inject(this, view);
        this.manager = new ViewPagerManager(
            this.viewPager,
            this.getFragmentManager(),
            AuthFragmentFactory.getInstance(),
            AppConst.ViewPager.Auth.LENGTH
        );

        this.progress.setMax(AppConst.ViewPager.Auth.LENGTH - 1);

        this.viewPager.setPagerController(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /* Bind this to Activity as ViewPagerController*/
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((AuthActivity) activity).setViewPagerController(this);
    }
    @Override
    public void onDetach() {
        super.onDetach();
        ((AuthActivity) this.getActivity()).setViewPagerController(null);
    }

    /* ViewPagerController */
    @Override
    public Stack<Integer> getHistoryCopy() {
        return this.manager.getHistoryCopy();
    }

    @Override
    public int getPreviousPage() {
        return this.manager.getPreviousPage();
    }

    @Override
    public void setCurrentPage (int pageNum, boolean addToBackStack) {
        this.progress.setProgress(pageNum);
        this.manager.setCurrentPage(pageNum, addToBackStack);
        this.logoScaleAnimation(pageNum, false);
    }

    public void logoScaleAnimation(int pageNum, boolean bigger) {
        ValueAnimator logoAnim;
        ViewGroup.LayoutParams params = applicationLogo.getLayoutParams();
        if (pageNum == AppConst.ViewPager.Auth.SIGNUP_STEP1){
            ValueAnimator nameAnim;
            ViewGroup.LayoutParams stateNameParams = stateName.getLayoutParams();
            int height  = params.height, width = params.width;
            float sHeight = getResources().getDimension(R.dimen.baseline_x4);
            if (bigger) {
                logoAnim = ValueAnimator.ofFloat(1, 2);
                nameAnim = ValueAnimator.ofFloat(1, 0);
            } else {
                logoAnim = ValueAnimator.ofFloat(1, 0.5f);
                nameAnim = ValueAnimator.ofFloat(0, 1);
            }
            this.progress.setVisibility(bigger ? View.GONE : View.VISIBLE);

            logoAnim.addUpdateListener(
                animation -> {
                    params.height = (int) (height * (float) animation.getAnimatedValue());
                    params.width = (int) (width * (float) animation.getAnimatedValue());
                    applicationLogo.setLayoutParams(params);
                });
            nameAnim.addUpdateListener(
                animation -> {
                    stateNameParams.height = (int) (sHeight * (float) animation.getAnimatedValue());
                    stateName.setLayoutParams(stateNameParams);
                    stateName.setAlpha((float)animation.getAnimatedValue());
                }
            );
//            logoAnim.start();
            AnimatorSet set = new AnimatorSet();
            set.playTogether(logoAnim, nameAnim);
            set.start();
        }else if(pageNum == AppConst.ViewPager.Auth.SIGNIN){

        }
    }

    @Override
    public boolean popCurrentPage () {
        return this.manager.popCurrentPage();
    }

    @Override
    public boolean back() {
        if(this.progress.getProgress() > 0) this.progress.setProgress(this.progress.getProgress()-1);
        if (this.manager.controlTargetContains(this.manager.getCurrentPage())){
            this.popCurrentPage();
            return true;
        }
        this.logoScaleAnimation(this.manager.getCurrentPage(), true);
        return this.manager.back();
    }

    @Override
    public void addImeControlFragment(int page) {
        this.manager.addImeControlFragment(page);
    }

    @Override
    public boolean controlTargetContains(int number) {
        return this.manager.controlTargetContains(number);
    }

    @Override
    public int getCurrentPage() {
        return this.manager.getCurrentPage();
    }

}