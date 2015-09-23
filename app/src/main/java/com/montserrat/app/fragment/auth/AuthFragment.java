package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.activity.AuthActivity;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.view.viewpager.FlexibleViewPager;
import com.montserrat.utils.view.viewpager.ViewPagerController;
import com.montserrat.utils.view.viewpager.ViewPagerManager;
import com.squareup.picasso.Picasso;

import java.util.Stack;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

public class AuthFragment extends Fragment implements ViewPagerController {
    private ViewPagerManager manager;

    @InjectView(R.id.application_logo) protected ImageView applicationLogo;
    @InjectView(R.id.signup_progress) protected ProgressBar progress;
    @InjectView(R.id.auth_viewpager) protected FlexibleViewPager viewPager;
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
        Picasso.with(this.getActivity()).load(R.drawable.ic_light_edit).transform(new ColorFilterTransformation(this.getResources().getColor(R.color.fg_accent))).into(applicationLogo);
        this.progress.setMax(AppConst.ViewPager.Auth.LENGTH - 1);

        this.viewPager.setPagerController(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
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
        this.progress.setProgress(pageNum - 1);
        this.manager.setCurrentPage(pageNum, addToBackStack);
    }

    @Override
    public boolean popCurrentPage () {
        if(this.progress.getProgress() > 0) this.progress.setProgress(this.progress.getProgress() - 1);
        return this.manager.popCurrentPage();
    }

    @Override
    public boolean back() {
        if(this.progress.getProgress() > 0) this.progress.setProgress(this.progress.getProgress() - 1);
        if (this.manager.controlTargetContains(this.manager.getCurrentPage())){
            this.popCurrentPage();
            return true;
        }
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